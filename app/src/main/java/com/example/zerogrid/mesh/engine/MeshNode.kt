package com.example.zerogrid.mesh.engine

/**
 * Represents a peer node in the ZeroGrid Mesh Network.
 */
data class MeshNode(
    val nodeId: String,
    val alias: String,
    var rssi: Int = 0,
    var transportType: String = TRANSPORT_BLE,
    var lastSeenTimestamp: Long = System.currentTimeMillis(),
    var hopDistance: Int = 1,
    var isDirectNeighbor: Boolean = true
) {
    companion object {
        const val TRANSPORT_BLE = "BLE"
        const val TRANSPORT_WIFI_DIRECT = "Wi-Fi Direct"
        const val TRANSPORT_MULTI_HOP = "Multi-Hop Mesh"
    }
}
