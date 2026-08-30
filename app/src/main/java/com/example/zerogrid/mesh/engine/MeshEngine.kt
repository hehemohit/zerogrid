package com.example.zerogrid.mesh.engine

import android.content.Context
import android.util.Log
import com.example.zerogrid.mesh.transport.BleMeshDriver
import com.example.zerogrid.mesh.transport.WifiDirectMeshDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Top-level Mesh Engine facade for ZeroGrid.
 * Coordinates transports, routing, peer discovery state, and exposes reactive StateFlows for UI.
 */
class MeshEngine private constructor(context: Context) {

    companion object {
        private const val TAG = "MeshEngine"

        @Volatile
        private var INSTANCE: MeshEngine? = null

        fun getInstance(context: Context): MeshEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MeshEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val localNodeId: String = "NODE-" + UUID.randomUUID().toString().take(8)
    private val peerTable = PeerTable()
    private val routingEngine = MeshRoutingEngine(localNodeId)

    private val bleDriver: BleMeshDriver = BleMeshDriver(context, localNodeId)
    private val wifiDirectDriver: WifiDirectMeshDriver = WifiDirectMeshDriver(context, localNodeId)

    private val transports = listOf(bleDriver, wifiDirectDriver)

    private val _connectedPeers = MutableStateFlow<List<MeshNode>>(emptyList())
    val connectedPeers: StateFlow<List<MeshNode>> = _connectedPeers.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<MeshPacket>>(emptyList())
    val receivedMessages: StateFlow<List<MeshPacket>> = _receivedMessages.asStateFlow()

    private val _sosAlerts = MutableStateFlow<List<MeshPacket>>(emptyList())
    val sosAlerts: StateFlow<List<MeshPacket>> = _sosAlerts.asStateFlow()

    private val _isMeshActive = MutableStateFlow(false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        transports.forEach { transport ->
            routingEngine.registerTransport(transport)

            scope.launch {
                transport.peerDiscoveryFlow.collect { peer ->
                    peerTable.updateOrAddPeer(peer)
                    _connectedPeers.value = peerTable.getAllPeers()
                }
            }
        }

        scope.launch {
            routingEngine.incomingPackets.collect { packet ->
                handleIncomingPacket(packet)
            }
        }
    }

    fun startMesh() {
        Log.d(TAG, "Starting ZeroGrid Mesh Engine (Node ID: $localNodeId)")
        transports.forEach { it.startDiscovery() }
        _isMeshActive.value = true
    }

    fun stopMesh() {
        Log.d(TAG, "Stopping ZeroGrid Mesh Engine")
        transports.forEach { it.stopDiscovery() }
        _isMeshActive.value = false
    }

    fun sendDirectMessage(recipientId: String, text: String): MeshPacket {
        val packet = MeshPacket(
            senderId = localNodeId,
            recipientId = recipientId,
            type = PacketType.DIRECT_MESSAGE,
            payload = text,
        )
        routingEngine.sendOutboundPacket(packet)
        return packet
    }

    fun broadcastChannelMessage(channelName: String, text: String): MeshPacket {
        val payload = "[$channelName] $text"
        val packet = MeshPacket(
            senderId = localNodeId,
            recipientId = MeshPacket.BROADCAST_ADDRESS,
            type = PacketType.CHANNEL_BROADCAST,
            payload = payload
        )
        routingEngine.sendOutboundPacket(packet)
        return packet
    }

    fun triggerSosBeacon(category: String, message: String, lat: Double? = null, lon: Double? = null): MeshPacket {
        val payload = "Category: $category | Msg: $message | Lat: ${lat ?: 0.0}, Lon: ${lon ?: 0.0}"
        val packet = MeshPacket(
            senderId = localNodeId,
            recipientId = MeshPacket.BROADCAST_ADDRESS,
            ttl = 10,
            type = PacketType.SOS_BEACON,
            payload = payload
        )
        routingEngine.sendOutboundPacket(packet)
        handleIncomingPacket(packet)
        return packet
    }

    private fun handleIncomingPacket(packet: MeshPacket) {
        when (packet.type) {
            PacketType.SOS_BEACON -> {
                val current = _sosAlerts.value.toMutableList()
                current.add(0, packet)
                _sosAlerts.value = current
            }
            PacketType.DIRECT_MESSAGE, PacketType.CHANNEL_BROADCAST -> {
                val current = _receivedMessages.value.toMutableList()
                current.add(packet)
                _receivedMessages.value = current
            }
            else -> {
                Log.d(TAG, "Received packet type ${packet.type}")
            }
        }
    }
}
