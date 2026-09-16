package com.zerogrid.mesh.app.mesh.transport

import com.zerogrid.mesh.app.mesh.engine.MeshNode
import com.zerogrid.mesh.app.mesh.engine.MeshPacket
import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface defining radio transport engines for ZeroGrid (e.g. BLE, Wi-Fi Direct).
 */
interface MeshTransport {
    val transportName: String
    val isRunning: Boolean
    val packetFlow: SharedFlow<MeshPacket>
    val peerDiscoveryFlow: SharedFlow<MeshNode>

    fun startDiscovery()
    fun stopDiscovery()
    fun sendPacket(packet: MeshPacket, targetPeerId: String? = null): Boolean
}
