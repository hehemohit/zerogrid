package com.example.zerogrid.map.data

import android.util.Log
import com.example.zerogrid.map.domain.MovementState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents the most-recent known location of a peer node.
 */
data class LocationRecord(
    val peerId: String,
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Float,
    val timestamp: Long,
    val batteryPercent: Int,
    val movementState: MovementState,
    val hopCount: Int
)

/**
 * In-memory peer location cache with bounded capacity, replay-attack protection,
 * and write-buffered persistence to [ZeroGridDatabase].
 *
 * Features:
 * - Bounded to [MAX_PEERS] entries; oldest-timestamp peer is evicted when full.
 * - Rejects packets where timestamp <= existing record's timestamp (replay protection).
 * - Batches DB inserts via a [Channel] every [FLUSH_INTERVAL_MS] ms to reduce write pressure.
 * - Hydrates from the last [HYDRATION_WINDOW_MS] of DB history on cold start.
 */
class PeerLocationCache(
    private val dao: LocationHistoryDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "PeerLocationCache"
        private const val MAX_PEERS = 200
        private const val FLUSH_INTERVAL_MS = 3_000L
        private const val HYDRATION_WINDOW_MS = 30 * 60 * 1_000L // 30 minutes
    }

    private val map = ConcurrentHashMap<String, LocationRecord>(MAX_PEERS)
    private val writeChannel = Channel<LocationHistoryEntity>(capacity = 512)

    private val _locations = MutableStateFlow<Map<String, LocationRecord>>(emptyMap())
    val locations: StateFlow<Map<String, LocationRecord>> = _locations.asStateFlow()

    init {
        startFlushLoop()
        hydrate()
    }

    /**
     * Updates the cache with a new location record.
     *
     * @param packet    Decoded [LocationPacket] from the mesh.
     * @param hopCount  Number of mesh hops the packet traversed.
     * @return          True if the record was accepted (not a replay or duplicate).
     */
    fun update(packet: LocationPacket, hopCount: Int): Boolean {
        val existing = map[packet.peerId]

        // Replay / out-of-order protection
        if (existing != null && packet.timestamp <= existing.timestamp) {
            Log.d(TAG, "Rejected stale/replay packet from ${packet.peerId} (t=${packet.timestamp} <= ${existing.timestamp})")
            return false
        }

        // Capacity enforcement: evict oldest if at limit
        if (map.size >= MAX_PEERS && !map.containsKey(packet.peerId)) {
            val oldest = map.values.minByOrNull { it.timestamp }
            if (oldest != null) {
                map.remove(oldest.peerId)
                Log.d(TAG, "Evicted oldest peer ${oldest.peerId} to make room")
            }
        }

        val record = LocationRecord(
            peerId        = packet.peerId,
            lat           = packet.lat,
            lng           = packet.lng,
            accuracyMeters = packet.accuracyMeters,
            timestamp     = packet.timestamp,
            batteryPercent = packet.batteryPercent,
            movementState  = packet.movementState,
            hopCount       = hopCount
        )
        map[packet.peerId] = record
        _locations.value = HashMap(map)

        // Queue for async DB write
        scope.launch {
            writeChannel.trySend(
                LocationHistoryEntity(
                    peerId        = packet.peerId,
                    lat           = packet.lat,
                    lng           = packet.lng,
                    accuracyMeters = packet.accuracyMeters,
                    timestamp     = packet.timestamp,
                    hopCount      = hopCount
                )
            )
        }

        return true
    }

    /** Returns the latest [LocationRecord] for a peer, or null if unknown. */
    fun getRecord(peerId: String): LocationRecord? = map[peerId]

    /** Returns a snapshot of all known peer locations. */
    fun snapshot(): Map<String, LocationRecord> = HashMap(map)

    /** Retrieves the full movement trail (from DB) for a given peer since [sinceMs]. */
    suspend fun getTrail(peerId: String, sinceMs: Long): List<LocationHistoryEntity> =
        dao.getHistory(peerId, sinceMs)

    /** Flushes the write buffer to the database immediately. */
    suspend fun flushNow() {
        val pending = mutableListOf<LocationHistoryEntity>()
        while (true) {
            val item = writeChannel.tryReceive().getOrNull() ?: break
            pending.add(item)
        }
        if (pending.isNotEmpty()) {
            dao.insertAll(pending)
            Log.d(TAG, "Flushed ${pending.size} location records to DB")
        }
    }

    private fun startFlushLoop() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(FLUSH_INTERVAL_MS)
                flushNow()
            }
        }
    }

    private fun hydrate() {
        scope.launch {
            try {
                val sinceMs = System.currentTimeMillis() - HYDRATION_WINDOW_MS
                val recent = dao.getAllLatestSince(sinceMs)
                recent.forEach { entity ->
                    if (!map.containsKey(entity.peerId)) {
                        map[entity.peerId] = LocationRecord(
                            peerId        = entity.peerId,
                            lat           = entity.lat,
                            lng           = entity.lng,
                            accuracyMeters = entity.accuracyMeters,
                            timestamp     = entity.timestamp,
                            batteryPercent = -1,
                            movementState  = MovementState.UNKNOWN,
                            hopCount       = entity.hopCount
                        )
                    }
                }
                if (recent.isNotEmpty()) {
                    _locations.value = HashMap(map)
                    Log.d(TAG, "Hydrated ${recent.size} peers from DB")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Hydration failed", e)
            }
        }
    }
}
