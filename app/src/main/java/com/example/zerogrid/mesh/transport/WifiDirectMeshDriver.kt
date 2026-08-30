package com.example.zerogrid.mesh.transport

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.mesh.engine.MeshPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

/**
 * Wi-Fi Direct (P2P) Mesh Driver for ZeroGrid.
 * Manages Wi-Fi Direct peer discovery and TCP socket streaming for high-bandwidth P2P transfers.
 */
class WifiDirectMeshDriver(
    private val context: Context,
    localNodeId: String
) : MeshTransport {

    init {
        Log.d(TAG, "Initializing WifiDirectMeshDriver for $localNodeId")
    }

    companion object {
        private const val TAG = "WifiDirectMeshDriver"
        private const val SERVER_PORT = 8888
    }

    override val transportName: String = MeshNode.TRANSPORT_WIFI_DIRECT
    override var isRunning: Boolean = false
        private set

    private val _packetFlow = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    override val packetFlow: SharedFlow<MeshPacket> = _packetFlow.asSharedFlow()

    private val _peerDiscoveryFlow = MutableSharedFlow<MeshNode>(extraBufferCapacity = 64)
    override val peerDiscoveryFlow: SharedFlow<MeshNode> = _peerDiscoveryFlow.asSharedFlow()

    private val wifiP2pManager: WifiP2pManager? by lazy {
        try { context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager } catch (_: Exception) { null }
    }
    private var channel: WifiP2pManager.Channel? = null
    private var serverSocket: ServerSocket? = null
    private var groupOwnerAddress: String? = null
    private var isGroupOwner: Boolean = false

    private val peerSockets = ConcurrentHashMap<String, Socket>()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val p2pReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    wifiP2pManager?.requestPeers(channel) { peers ->
                        peers.deviceList.forEach { handlePeerDeviceDiscovered(it) }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    @Suppress("DEPRECATION")
                    val isConnected = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)?.isConnected == true
                    
                    if (isConnected) {
                        wifiP2pManager?.requestConnectionInfo(channel) { info ->
                            handleConnectionInfo(info)
                        }
                    } else {
                        groupOwnerAddress = null
                        isGroupOwner = false
                        clearPeerSockets()
                    }
                }
            }
        }
    }

    private fun handleConnectionInfo(info: WifiP2pInfo) {
        groupOwnerAddress = info.groupOwnerAddress?.hostAddress
        isGroupOwner = info.isGroupOwner
        Log.d(TAG, "Wi-Fi Direct connection info: GO=$isGroupOwner, GO_ADDR=$groupOwnerAddress")
    }

    private fun clearPeerSockets() {
        peerSockets.values.forEach { try { it.close() } catch (_: Exception) {} }
        peerSockets.clear()
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        try {
            if (wifiP2pManager == null) {
                Log.w(TAG, "Wi-Fi Direct P2P Manager not available")
                return
            }

            channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
            isRunning = true

            val filter = IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            }
            context.registerReceiver(p2pReceiver, filter)

            startServerSocketListener()

            wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Wi-Fi Direct peer discovery initiated")
                }

                override fun onFailure(reasonCode: Int) {
                    Log.e(TAG, "Wi-Fi Direct peer discovery failed code: $reasonCode")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Wi-Fi Direct discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        isRunning = false
        try {
            context.unregisterReceiver(p2pReceiver)
            wifiP2pManager?.stopPeerDiscovery(channel, null)
            serverSocket?.close()
            serverSocket = null
            clearPeerSockets()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Wi-Fi Direct discovery", e)
        }
    }

    override fun sendPacket(packet: MeshPacket, targetPeerId: String?): Boolean {
        if (!isRunning) return false

        scope.launch {
            val address = groupOwnerAddress ?: return@launch
            if (isGroupOwner) {
                // If I'm GO, I might have multiple clients. For Phase 1, we'll try to reach everyone in peerSockets
                peerSockets.values.forEach { transmitOverSocket(it, packet) }
            } else {
                // If I'm client, I send to GO
                transmitToAddress(address, packet)
            }
        }
        return true
    }

    private suspend fun transmitToAddress(address: String, packet: MeshPacket) = withContext(Dispatchers.IO) {
        try {
            var socket = peerSockets[address]
            if (socket == null || socket.isClosed) {
                socket = Socket()
                socket.connect(InetSocketAddress(address, SERVER_PORT), 5000)
                peerSockets[address] = socket
            }
            transmitOverSocket(socket, packet)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transmit to $address via TCP", e)
            peerSockets.remove(address)
        }
    }

    private suspend fun transmitOverSocket(socket: Socket, packet: MeshPacket) = withContext(Dispatchers.IO) {
        try {
            val output = DataOutputStream(socket.getOutputStream())
            val bytes = packet.toByteArray()
            output.writeInt(bytes.size)
            output.write(bytes)
            output.flush()
            Log.d(TAG, "Sent TCP packet to ${socket.inetAddress?.hostAddress} (${bytes.size} bytes)")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to socket", e)
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun startServerSocketListener() {
        scope.launch {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                Log.d(TAG, "Socket server listening on port $SERVER_PORT")
                while (isRunning && serverSocket?.isClosed == false) {
                    val socket = serverSocket?.accept() ?: break
                    val clientAddress = socket.inetAddress?.hostAddress
                    clientAddress?.let { peerSockets[it] = socket }
                    handleClientSocket(socket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server socket exception", e)
            }
        }
    }

    private fun handleClientSocket(socket: Socket) {
        scope.launch {
            try {
                val input = DataInputStream(socket.getInputStream())
                while (isRunning && !socket.isClosed) {
                    val length = try { input.readInt() } catch (_: Exception) { -1 }
                    if (length <= 0 || length > 1024 * 1024) break // Max 1MB packet
                    
                    val payload = ByteArray(length)
                    input.readFully(payload)
                    
                    val jsonStr = String(payload, Charsets.UTF_8)
                    val packet = MeshPacket.fromJson(jsonStr)
                    packet?.let { _packetFlow.emit(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading from client socket", e)
            } finally {
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    fun handlePeerDeviceDiscovered(device: WifiP2pDevice) {
        try {
            val deviceName = try { device.deviceName } catch (_: SecurityException) { null } ?: "Wi-Fi Peer"
            val deviceAddress = try { device.deviceAddress } catch (_: SecurityException) { null } ?: "00:00:00:00:00:00"

            val node = MeshNode(
                nodeId = deviceAddress,
                alias = deviceName.ifBlank { "Wi-Fi ${deviceAddress.takeLast(4)}" },
                rssi = -50,
                transportType = MeshNode.TRANSPORT_WIFI_DIRECT,
                lastSeenTimestamp = System.currentTimeMillis(),
                hopDistance = 1,
                isDirectNeighbor = true
            )
            scope.launch {
                _peerDiscoveryFlow.emit(node)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling discovered peer device", e)
        }
    }
}
