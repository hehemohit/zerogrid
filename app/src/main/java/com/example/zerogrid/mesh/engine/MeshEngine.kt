package com.example.zerogrid.mesh.engine

import android.content.Context
import android.util.Log
import com.example.zerogrid.messaging.MessageStore
import com.example.zerogrid.messaging.StoredMessage
import com.example.zerogrid.mesh.transport.BleMeshDriver
import com.example.zerogrid.mesh.transport.WifiDirectMeshDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

/**
 * Central Mesh Engine orchestrator for ZeroGrid.
 * Manages peer discovery, active transports (BLE & Wi-Fi Direct),
 * multi-hop store-and-forward routing, SOS beacons, channels, and persistent direct messaging.
 */
class MeshEngine private constructor(private val context: Context) {

    companion object {
        private const val TAG = "MeshEngine"
        private const val PREFS_NAME = "zerogrid_identity_prefs"
        private const val KEY_NODE_ID = "local_node_id"
        private const val KEY_DISPLAY_NAME = "display_name"

        @Volatile
        private var INSTANCE: MeshEngine? = null

        fun getInstance(context: Context): MeshEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MeshEngine(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun getOrGenerateLocalNodeId(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var id = prefs.getString(KEY_NODE_ID, null)
            if (id.isNullOrEmpty()) {
                val hex = UUID.randomUUID().toString().replace("-", "").take(8).lowercase()
                id = "NODE-$hex"
                prefs.edit().putString(KEY_NODE_ID, id).apply()
            }
            return id
        }

        private fun getOrGenerateDisplayName(context: Context, localNodeId: String): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var name = prefs.getString(KEY_DISPLAY_NAME, null)
            if (name.isNullOrEmpty()) {
                val btName = try {
                    val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                    bm?.adapter?.name
                } catch (_: Exception) { null }
                name = if (!btName.isNullOrBlank() && btName != "null") btName else android.os.Build.MODEL
                if (name.isNullOrBlank() || name == "null") name = "Node-${localNodeId.takeLast(4)}"
                prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
            }
            return name
        }
    }

    val localNodeId: String = getOrGenerateLocalNodeId(context)
    private val peerTable = PeerTable()
    private val routingEngine = MeshRoutingEngine(localNodeId)
    private val messageStore = MessageStore.getInstance(context)

    private val _displayName = MutableStateFlow(getOrGenerateDisplayName(context, localNodeId))
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val bleDriver: BleMeshDriver = BleMeshDriver(context, localNodeId, _displayName.value)
    private val wifiDirectDriver: WifiDirectMeshDriver = WifiDirectMeshDriver(context, localNodeId)

    private val transports = listOf(bleDriver, wifiDirectDriver)

    private val _connectedPeers = MutableStateFlow<List<MeshNode>>(emptyList())
    val connectedPeers: StateFlow<List<MeshNode>> = _connectedPeers.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<MeshPacket>>(emptyList())
    val receivedMessages: StateFlow<List<MeshPacket>> = _receivedMessages.asStateFlow()

    private val _sosAlerts = MutableStateFlow<List<MeshPacket>>(emptyList())
    val sosAlerts: StateFlow<List<MeshPacket>> = _sosAlerts.asStateFlow()

    private val _acknowledgedAlertIds = MutableStateFlow<Set<String>>(emptySet())
    val acknowledgedAlertIds: StateFlow<Set<String>> = _acknowledgedAlertIds.asStateFlow()

    private val _packetsRelayedCount = MutableStateFlow(0)
    val packetsRelayedCount: StateFlow<Int> = _packetsRelayedCount.asStateFlow()

    private val _isMeshActive = MutableStateFlow(value = false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive.asStateFlow()

    /**
     * In-memory conversation map: peerId -> list of StoredMessages.
     * Loaded from MessageStore at startup. Updated live as messages arrive or are sent.
     */
    private val _conversations = MutableStateFlow<Map<String, List<StoredMessage>>>(emptyMap())
    val conversations: StateFlow<Map<String, List<StoredMessage>>> = _conversations.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Load persisted conversations from disk into memory
        scope.launch {
            val allPeerIds = messageStore.getAllConversationPeerIds()
            val loaded = mutableMapOf<String, List<StoredMessage>>()
            allPeerIds.forEach { peerId ->
                loaded[peerId] = messageStore.getConversation(peerId)
            }
            _conversations.value = loaded
            Log.d(TAG, "Loaded ${loaded.size} conversations from MessageStore")
        }

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

    /** Returns the conversation history for a specific peer (live StateFlow slice). */
    fun getConversation(peerId: String): List<StoredMessage> {
        return _conversations.value[peerId] ?: emptyList()
    }

    /** All peer IDs that have at least one stored message, sorted by most recent. */
    fun getConversationPeerIds(): List<String> = messageStore.getAllConversationPeerIds()

    fun setDisplayName(name: String) {
        if (name.isNotBlank()) {
            val trimmed = name.trim()
            _displayName.value = trimmed
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_DISPLAY_NAME, trimmed).apply()
            bleDriver.updateDisplayName(trimmed)
        }
    }

    fun acknowledgeSosAlert(packetId: String) {
        _acknowledgedAlertIds.value = _acknowledgedAlertIds.value + packetId
    }

    fun getPublicKeyFingerprint(): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(localNodeId.toByteArray(Charsets.UTF_8))
            hash.take(16).joinToString(":") { "%02X".format(it) }
        } catch (_: Exception) {
            "ED:25:51:90:7A:42:00:FF:AA:BB:CC:DD:EE:FF:11:22"
        }
    }

    fun getBleRunning(): Boolean = bleDriver.isRunning
    fun getWifiDirectRunning(): Boolean = wifiDirectDriver.isRunning

    fun startMesh() {
        Log.d(TAG, "Starting ZeroGrid Mesh Engine (Node ID: $localNodeId, Name: ${_displayName.value})")
        transports.forEach { it.startDiscovery() }
        _isMeshActive.value = true
    }

    fun stopMesh() {
        Log.d(TAG, "Stopping ZeroGrid Mesh Engine")
        transports.forEach {
            it.stopDiscovery()
            routingEngine.unregisterTransport(it)
        }
        _isMeshActive.value = false
    }

    /**
     * Send a direct message to a peer. Also stores it locally as a sent message
     * so the conversation history is immediately visible without waiting for echo.
     */
    fun sendDirectMessage(recipientId: String, text: String): MeshPacket {
        val packet = MeshPacket(
            senderId = localNodeId,
            recipientId = recipientId,
            type = PacketType.DIRECT_MESSAGE,
            payload = text,
        )
        routingEngine.sendOutboundPacket(packet)

        // Persist the sent message to local store immediately
        val stored = StoredMessage(
            id = packet.packetId,
            senderId = localNodeId,
            recipientId = recipientId,
            text = text,
            timestamp = packet.timestamp,
            hopCount = 0,
            isMine = true
        )
        persistAndUpdateConversation(recipientId, stored)

        return packet
    }

    fun broadcastChannelMessage(channelName: String, text: String): MeshPacket {
        val payload = "[$channelName] $text"
        val packet = MeshPacket(
            senderId = localNodeId,
            recipientId = MeshPacket.BROADCAST_ADDRESS,
            type = PacketType.CHANNEL_BROADCAST,
            payload = payload,
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
            payload = payload,
        )
        // Transmit to all mesh peers via transports
        routingEngine.sendOutboundPacket(packet)
        // Add to local sosAlerts for display on THIS device's SOS center
        val current = _sosAlerts.value.toMutableList()
        if (current.none { it.packetId == packet.packetId }) {
            current.add(0, packet)
            _sosAlerts.value = current
        }
        return packet
    }

    private fun handleIncomingPacket(packet: MeshPacket) {
        when (packet.type) {
            PacketType.PEER_DISCOVERY -> {
                val peerAlias = if (packet.payload.isNotBlank()) packet.payload else "Peer ${packet.senderId.takeLast(4)}"
                val peerNode = MeshNode(
                    nodeId = packet.senderId,
                    alias = peerAlias,
                    rssi = -30,
                    transportType = MeshNode.TRANSPORT_BLE,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    hopDistance = packet.hopCount.coerceAtLeast(1),
                    isDirectNeighbor = packet.hopCount <= 1
                )
                peerTable.updateOrAddPeer(peerNode)
                _connectedPeers.value = peerTable.getAllPeers()
            }
            PacketType.SOS_BEACON -> {
                // Only process alerts from OTHER nodes — we already stored our own in triggerSosBeacon
                if (packet.senderId == localNodeId) return
                val current = _sosAlerts.value.toMutableList()
                if (current.none { it.packetId == packet.packetId }) {
                    current.add(0, packet)
                    _sosAlerts.value = current
                    // Show system heads-up notification only for REMOTE beacons
                    com.example.zerogrid.service.MeshForegroundService.showSosNotification(
                        context,
                        packet.senderId,
                        packet.payload
                    )
                }
            }
            PacketType.DIRECT_MESSAGE -> {
                // Update in-memory receivedMessages flow
                val current = _receivedMessages.value.toMutableList()
                if (current.none { it.packetId == packet.packetId }) {
                    current.add(packet)
                    _receivedMessages.value = current
                }
                // Persist to MessageStore keyed by sender
                val stored = StoredMessage(
                    id = packet.packetId,
                    senderId = packet.senderId,
                    recipientId = packet.recipientId,
                    text = packet.payload,
                    timestamp = packet.timestamp,
                    hopCount = packet.hopCount,
                    isMine = false
                )
                persistAndUpdateConversation(packet.senderId, stored)
            }
            PacketType.CHANNEL_BROADCAST -> {
                val current = _receivedMessages.value.toMutableList()
                if (current.none { it.packetId == packet.packetId }) {
                    current.add(packet)
                    _receivedMessages.value = current
                }
            }
            else -> {
                Log.d(TAG, "Received packet type ${packet.type}")
            }
        }
        if (packet.senderId != localNodeId) {
            _packetsRelayedCount.value = _packetsRelayedCount.value + 1
        }
    }

    /** Persist a message to disk and update the in-memory _conversations StateFlow. */
    private fun persistAndUpdateConversation(peerId: String, msg: StoredMessage) {
        messageStore.appendMessage(peerId, msg)
        val updated = _conversations.value.toMutableMap()
        val existing = updated[peerId]?.toMutableList() ?: mutableListOf()
        if (existing.none { it.id == msg.id }) {
            existing.add(msg)
        }
        updated[peerId] = existing
        _conversations.value = updated
    }
}
