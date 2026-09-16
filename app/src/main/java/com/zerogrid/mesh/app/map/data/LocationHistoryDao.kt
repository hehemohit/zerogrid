package com.zerogrid.mesh.app.map.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room DAO for the peer_locations table providing batch insert, latest-fix query,
 * recent history, and TTL-based pruning.
 */
@Dao
interface LocationHistoryDao {

    /** Batch-inserts a list of location history entities. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(locations: List<LocationHistoryEntity>)

    /** Returns the most recent fix for a specific peer, or null if none found. */
    @Query("SELECT * FROM peer_locations WHERE peerId = :peerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(peerId: String): LocationHistoryEntity?

    /**
     * Returns all fixes for a specific peer since [sinceMs] (Unix epoch ms),
     * ordered chronologically for movement trail rendering.
     */
    @Query("SELECT * FROM peer_locations WHERE peerId = :peerId AND timestamp >= :sinceMs ORDER BY timestamp ASC")
    suspend fun getHistory(peerId: String, sinceMs: Long): List<LocationHistoryEntity>

    /**
     * Returns the most recent fix for every peer that has a fix newer than [sinceMs].
     * Used to cold-start hydrate the [PeerLocationCache].
     */
    @Query("""
        SELECT * FROM peer_locations
        WHERE timestamp >= :sinceMs
        AND id IN (
            SELECT MAX(id) FROM peer_locations WHERE timestamp >= :sinceMs GROUP BY peerId
        )
    """)
    suspend fun getAllLatestSince(sinceMs: Long): List<LocationHistoryEntity>

    /** Deletes all location history records older than [cutoffMs] (Unix epoch ms). */
    @Query("DELETE FROM peer_locations WHERE timestamp < :cutoffMs")
    suspend fun pruneOlderThan(cutoffMs: Long): Int

    /** Returns the total number of stored location records. */
    @Query("SELECT COUNT(*) FROM peer_locations")
    suspend fun count(): Int
}
