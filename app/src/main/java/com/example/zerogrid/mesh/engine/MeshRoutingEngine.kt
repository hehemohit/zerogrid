package com.example.zerogrid.mesh.engine

import android.util.Log
import com.example.zerogrid.mesh.transport.MeshTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Multi-hop Store-and-Forward Routing Coordinator for ZeroGrid.
 * Implements deduplication, local delivery, and controlled packet relay across transports.
 */
class MeshRoutingEngine(
    private val localNodeId: String,
    private val deduplicationCache: DeduplicationCache = DeduplicationCache()
) {
    companion object {
        private const val TAG = "MeshRoutingEngine"
    }

    private val activeTransports = mutableListOf<MeshTransport>()

    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 128)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

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
            Log.d(TAG, "Dropped duplicate packet: ${packet.packetId}")
            return
        }

        Log.d(
            TAG,
            "Processing packet ${packet.packetId} from ${packet.senderId} (TTL=${packet.ttl}, Hops=${packet.hopCount})"
        )

        // 2. Check local delivery
        val isTargetedToMe = packet.recipientId == localNodeId
        val isBroadcast = packet.recipientId == MeshPacket.BROADCAST_ADDRESS

        if (isTargetedToMe || isBroadcast) {
            scope.launch {
                _incomingPackets.emit(packet)
            }
        }

        // 3. Multi-Hop Forwarding & Relay
        val shouldForward = ((isBroadcast || !isTargetedToMe) && packet.ttl > 1)
        if (shouldForward) {
            val relayedPacket = packet.copy(
                ttl = packet.ttl - 1,
                hopCount = packet.hopCount + 1
            )

            Log.d(
                TAG,
                "Relaying packet ${relayedPacket.packetId} to neighbor nodes (Remaining TTL=${relayedPacket.ttl})"
            )

            activeTransports.forEach { transport ->
                if (transport != sourceTransport && transport.isRunning) {
                    transport.sendPacket(relayedPacket)
                }
            }
        }
    }

    fun sendOutboundPacket(packet: MeshPacket): Boolean {
        // Record local packet in deduplication cache
        deduplicationCache.isDuplicateAndRecord(packet.packetId)

        var sentCount = 0
        activeTransports.forEach { transport ->
            if (transport.isRunning) {
                if (transport.sendPacket(packet)) {
                    sentCount++
                }
            }
        }
        return sentCount > 0
    }
}
