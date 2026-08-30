package com.example.zerogrid.mesh.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe dynamic peer discovery registry and dynamic routing table.
 */
class PeerTable {

    private val peers = ConcurrentHashMap<String, MeshNode>()

    fun updateOrAddPeer(node: MeshNode) {
        val existing = peers[node.nodeId]
        if (existing == null) {
            peers[node.nodeId] = node
        } else {
            existing.lastSeenTimestamp = System.currentTimeMillis()
            existing.rssi = node.rssi
            if (node.alias.isNotBlank() && !node.alias.startsWith("Peer ") && node.alias != "Peer") {
                existing.alias = node.alias
            }
            if (node.hopDistance < existing.hopDistance) {
                existing.hopDistance = node.hopDistance
                existing.transportType = node.transportType
            }
        }
    }

    fun removePeer(nodeId: String) {
        peers.remove(nodeId)
    }

    fun getPeer(nodeId: String): MeshNode? {
        return peers[nodeId]
    }

    fun getAllPeers(): List<MeshNode> {
        return peers.values.sortedByDescending { it.lastSeenTimestamp }
    }

    fun getDirectNeighbors(): List<MeshNode> {
        return peers.values.filter { it.isDirectNeighbor }.sortedByDescending { it.rssi }
    }

    fun pruneStalePeers(staleThresholdMs: Long = 60_000) {
        val now = System.currentTimeMillis()
        peers.entries.removeIf { entry -> (now - entry.value.lastSeenTimestamp) > staleThresholdMs }
    }

    fun clear() {
        peers.clear()
    }
}
