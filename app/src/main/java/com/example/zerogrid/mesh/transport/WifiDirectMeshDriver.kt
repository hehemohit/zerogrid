package com.example.zerogrid.mesh.transport

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * Wi-Fi Direct (P2P) Mesh Driver for ZeroGrid.
 * Manages Wi-Fi Direct peer discovery and TCP socket streaming for high-bandwidth P2P transfers.
 */
class WifiDirectMeshDriver(
    private val context: Context,
    private val localNodeId: String
) : MeshTransport {

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

    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        try {
            if (wifiP2pManager == null) {
                Log.w(TAG, "Wi-Fi Direct P2P Manager not available")
                return
            }

            channel = wifiP2pManager?.initialize(context, context.mainLooper, null)
            isRunning = true

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
            wifiP2pManager?.stopPeerDiscovery(channel, null)
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Wi-Fi Direct discovery", e)
        }
    }

    override fun sendPacket(packet: MeshPacket, targetPeerId: String?): Boolean {
        if (!isRunning) return false

        scope.launch {
            _packetFlow.emit(packet)
        }
        return true
    }

    private fun startServerSocketListener() {
        scope.launch {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                Log.d(TAG, "Socket server listening on port $SERVER_PORT")
                while (isRunning && serverSocket?.isClosed == false) {
                    val socket = serverSocket?.accept() ?: break
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
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val jsonLine = reader.readLine()
                if (!jsonLine.isNullOrBlank()) {
                    val packet = MeshPacket.fromJson(jsonLine)
                    packet?.let { _packetFlow.emit(it) }
                }
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error reading from client socket", e)
            }
        }
    }

    fun handlePeerDeviceDiscovered(device: WifiP2pDevice) {
        try {
            val deviceName = try { device.deviceName } catch (e: SecurityException) { null } ?: "Wi-Fi Peer"
            val deviceAddress = try { device.deviceAddress } catch (e: SecurityException) { null } ?: "00:00:00:00:00:00"

            val node = MeshNode(
                nodeId = deviceAddress,
                alias = if (deviceName.isNotBlank()) deviceName else "Wi-Fi ${deviceAddress.takeLast(4)}",
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
