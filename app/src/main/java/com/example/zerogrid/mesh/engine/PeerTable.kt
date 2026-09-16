package com.example.zerogrid.mesh.engine

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe dynamic peer discovery registry and dynamic routing table.
 * Implements multi-interface deduplication (BLE vs Wi-Fi Direct), automatic RSSI signal
 * evaluation, and canonical identity merging.
 */
class PeerTable(var localNodeId: String? = null) {

    private val peers = ConcurrentHashMap<String, MeshNode>()

    /**
     * Updates or inserts a peer node. If the device was previously observed on another
     * physical interface (e.g. BLE vs Wi-Fi), merges the profiles into a single logical
     * peer and automatically selects whichever transport currently offers the stronger RSSI signal.
     */
    fun updateOrAddPeer(node: MeshNode) {
        val myId = localNodeId
        if (myId != null) {
            val mySuffix = myId.removePrefix("NODE-")
            if (node.nodeId.equals(myId, ignoreCase = true) ||
                node.nodeId.removePrefix("NODE-").equals(mySuffix, ignoreCase = true) ||
                node.alias.equals(android.os.Build.MODEL, ignoreCase = true)) {
                return // Guard: Never register self as a peer
            }
        }

        val existing = peers[node.nodeId]
        if (existing == null) {
            if (node.transportType == MeshNode.TRANSPORT_BLE) {
                node.bleRssi = node.rssi
                node.availableTransports.add(MeshNode.TRANSPORT_BLE)
            } else if (node.transportType == MeshNode.TRANSPORT_WIFI_DIRECT) {
                node.wifiRssi = node.rssi
                node.availableTransports.add(MeshNode.TRANSPORT_WIFI_DIRECT)
            }
            node.availableTransports.addAll(node.availableTransports)
            peers[node.nodeId] = node
        } else {
            existing.lastSeenTimestamp = System.currentTimeMillis()

            // Merge interfaces and update per-interface signal strengths
            if (node.transportType == MeshNode.TRANSPORT_BLE) {
                existing.bleRssi = node.rssi
                existing.availableTransports.add(MeshNode.TRANSPORT_BLE)
            } else if (node.transportType == MeshNode.TRANSPORT_WIFI_DIRECT) {
                existing.wifiRssi = node.rssi
                existing.availableTransports.add(MeshNode.TRANSPORT_WIFI_DIRECT)
            }
            existing.availableTransports.addAll(node.availableTransports)

            // Multi-interface deduplication: Automatically route through the interface with the stronger signal
            val bleSignal = existing.bleRssi ?: -999
            val wifiSignal = existing.wifiRssi ?: -999

            if (wifiSignal > bleSignal && wifiSignal > -900) {
                existing.transportType = MeshNode.TRANSPORT_WIFI_DIRECT
                existing.rssi = wifiSignal
            } else if (bleSignal > -900) {
                existing.transportType = MeshNode.TRANSPORT_BLE
                existing.rssi = bleSignal
            } else {
                existing.rssi = node.rssi
                existing.transportType = node.transportType
            }

            // Update alias if incoming is a real custom display name
            if (isRealDisplayName(node.alias)) {
                existing.alias = node.alias
            } else if (!isRealDisplayName(existing.alias) && node.alias.isNotBlank()) {
                existing.alias = node.alias
            }

            if (node.hopDistance < existing.hopDistance) {
                existing.hopDistance = node.hopDistance
            }
        }
    }

    /**
     * Checks whether an alias is a user-chosen display name rather than a system fallback.
     */
    fun isRealDisplayName(alias: String): Boolean {
        if (alias.isBlank()) return false
        if (alias.startsWith("Peer ") || alias == "Peer") return false
        if (alias.startsWith("Android_")) return false
        if (alias.startsWith("Wi-Fi ")) return false
        if (alias.contains(":") && alias.length >= 17) return false // MAC address
        return true
    }

    /**
     * Merges a temporary MAC-addressed peer entry into its canonical logical Node ID.
     */
    fun mergePeer(fromNodeId: String, toNodeId: String) {
        if (fromNodeId.equals(toNodeId, ignoreCase = true)) return
        val old = peers.remove(fromNodeId) ?: return
        val target = peers[toNodeId]
        if (target != null) {
            if (isRealDisplayName(old.alias) && !isRealDisplayName(target.alias)) {
                target.alias = old.alias
            }
            if (old.wifiRssi != null) target.wifiRssi = old.wifiRssi
            if (old.bleRssi != null) target.bleRssi = old.bleRssi
            target.availableTransports.addAll(old.availableTransports)
            target.rssi = target.getBestSignalRssi()
        }
    }

    fun removePeer(nodeId: String) {
        peers.remove(nodeId)
    }

    fun getPeer(nodeId: String): MeshNode? {
        return peers[nodeId]
    }

    fun getAllPeers(): List<MeshNode> {
        val myId = localNodeId
        return peers.values
            .filter { peer ->
                if (myId != null) {
                    !peer.nodeId.equals(myId, ignoreCase = true) &&
                    !peer.nodeId.removePrefix("NODE-").equals(myId.removePrefix("NODE-"), ignoreCase = true) &&
                    !peer.alias.equals(android.os.Build.MODEL, ignoreCase = true)
                } else true
            }
            .sortedByDescending { it.lastSeenTimestamp }
    }

    fun getDirectNeighbors(): List<MeshNode> {
        val myId = localNodeId
        return peers.values
            .filter { peer ->
                val isNotSelf = if (myId != null) {
                    !peer.nodeId.equals(myId, ignoreCase = true) &&
                    !peer.nodeId.removePrefix("NODE-").equals(myId.removePrefix("NODE-"), ignoreCase = true) &&
                    !peer.alias.equals(android.os.Build.MODEL, ignoreCase = true)
                } else true
                peer.isDirectNeighbor && isNotSelf
            }
            .sortedByDescending { it.rssi }
    }

    fun pruneStalePeers(staleThresholdMs: Long = 60_000) {
        val now = System.currentTimeMillis()
        peers.entries.removeIf { entry -> (now - entry.value.lastSeenTimestamp) > staleThresholdMs }
    }

    fun clear() {
        peers.clear()
    }
}
