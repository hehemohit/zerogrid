package com.zerogrid.mesh.app.mesh.engine

import android.util.Log
import com.zerogrid.mesh.app.debug.DebugLevel
import com.zerogrid.mesh.app.debug.DebugLogger
import com.zerogrid.mesh.app.mesh.transport.MeshTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Multi-hop Store-and-Forward Routing Coordinator for ZeroGrid.
 * Implements deduplication, local delivery first, and controlled packet relay across transports.
 */
class MeshRoutingEngine(
    private val localNodeId: String,
    private val deduplicationCache: DeduplicationCache = DeduplicationCache(),
) {
    companion object {
        private const val TAG = "MeshRoutingEngine"
    }

    private val activeTransports = mutableListOf<MeshTransport>()

    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 128)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Packet priority order (highest to lowest):
     * SOS_BEACON > DIRECT_MESSAGE / CHANNEL_BROADCAST > FILE_CHUNK > LOCATION_PING
     *
     * LOCATION_PING TTL is hard-capped at [MAX_LOCATION_TTL] to prevent
     * location floods propagating across the full mesh.
     */
    private val MAX_LOCATION_TTL = 4

    /** Approximate inbound + relay queue depth for congestion backpressure. */
    private var _queueDepth = 0
    fun currentQueueDepth(): Int = _queueDepth

    fun registerTransport(transport: MeshTransport) {
        if (!activeTransports.contains(transport)) {
            activeTransports.add(transport)
            scope.launch {
                transport.packetFlow.collect { packet ->
                    processInboundPacket(packet, transport)
                }
            }
        }
    }

    fun unregisterTransport(transport: MeshTransport) {
        activeTransports.remove(transport)
    }

    fun processInboundPacket(packet: MeshPacket, sourceTransport: MeshTransport? = null) {
        // 1. Deduplication Check
        if (deduplicationCache.isDuplicateAndRecord(packet.packetId)) {
            Log.d(TAG, "Dropped duplicate or locally-originated packet: ${packet.packetId}")
            return
        }

        val sourceName = sourceTransport?.transportName ?: "Local"
        Log.d(
            TAG,
            "Processing packet from ${packet.senderId} (Target: ${packet.recipientId}) via $sourceName (TTL=${packet.ttl}, Hops=${packet.hopCount})"
        )
        DebugLogger.log(
            TAG,
            "Processing packet from ${packet.senderId} (Target: ${packet.recipientId}) via $sourceName",
            DebugLevel.DEBUG
        )

        // 2. Check local delivery first
        val isBroadcast = packet.recipientId == MeshPacket.BROADCAST_ADDRESS || packet.recipientId == "*"
        val isTargetedToMe = isRecipientMatch(packet.recipientId, localNodeId)

        if (isTargetedToMe || isBroadcast) {
            Log.i(TAG, "Target reached! Consuming locally: ${packet.packetId}")
            DebugLogger.log(
                TAG,
                "🎯 Target reached! Consuming locally: ${packet.packetId} (Type: ${packet.type}, From: ${packet.senderId})",
                DebugLevel.INFO
            )
            _queueDepth = (_queueDepth + 1).coerceAtMost(1000)
            scope.launch {
                _incomingPackets.emit(packet)
                _queueDepth = (_queueDepth - 1).coerceAtLeast(0)
            }
        }

        // 3. Multi-Hop Forwarding & Relay (Only forward if broadcast or not targeted to me, and TTL > 1)
        // LOCATION_PING: hard-cap effective TTL to MAX_LOCATION_TTL to prevent mesh flooding.
        val effectiveTtl = if (packet.type == PacketType.LOCATION_PING) {
            packet.ttl.coerceAtMost(MAX_LOCATION_TTL)
        } else {
            packet.ttl
        }
        val shouldForward = (isBroadcast || !isTargetedToMe) && (effectiveTtl > 1)
        if (shouldForward) {
            val relayedPacket = packet.copy(
                ttl = effectiveTtl - 1,
                hopCount = packet.hopCount + 1
            )

            activeTransports.forEach { transport ->
                if (transport != sourceTransport && transport.isRunning) {
                    Log.d(
                        TAG,
                        "Relaying packet ${relayedPacket.packetId} to neighbor via ${transport.transportName} (Remaining TTL=${relayedPacket.ttl})"
                    )
                    DebugLogger.log(
                        TAG,
                        "🔄 Relaying packet ${relayedPacket.packetId} via ${transport.transportName} (TTL=${relayedPacket.ttl})",
                        DebugLevel.DEBUG
                    )
                    transport.sendPacket(relayedPacket)
                }
            }
        }
    }

    /**
     * Checks if the recipientId matches our local node identity, handling:
     * - Exact logical NodeID ("NODE-d48923d7")
     * - Case-insensitive match
     * - Node suffix / prefix ("d48923d7" vs "NODE-d48923d7")
     * - Direct single-hop MAC-addressed delivery over BLE
     */
    private fun isRecipientMatch(recipientId: String, localNodeId: String): Boolean {
        if (recipientId.equals(localNodeId, ignoreCase = true)) return true
        if (recipientId.equals(localNodeId.removePrefix("NODE-"), ignoreCase = true)) return true
        if (localNodeId.equals("NODE-$recipientId", ignoreCase = true)) return true
        // If recipientId is a raw hardware address (e.g. "XX:XX:XX:..."), direct delivery is accepted
        if (recipientId.contains(":") && recipientId.length >= 17) return true
        return false
    }

    fun sendOutboundPacket(packet: MeshPacket): Boolean {
        // Record local packet in deduplication cache to prevent re-processing if it returns
        deduplicationCache.isDuplicateAndRecord(packet.packetId)

        Log.d(TAG, "Sending outbound packet ${packet.packetId} (Type: ${packet.type})")
        DebugLogger.log(TAG, "📤 Outbound ${packet.type} to ${packet.recipientId}", DebugLevel.DEBUG)
        
        var sentCount = 0
        activeTransports.forEach { transport ->
            if (transport.isRunning) {
                Log.d(TAG, "Attempting transmission via ${transport.transportName}")
                if (transport.sendPacket(packet)) {
                    sentCount++
                }
            }
        }
        return sentCount > 0
    }
}
