package com.example.zerogrid.map.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for storing historical peer location fixes.
 * Indexed on [peerId] for conversation lookup and [timestamp] for range-based pruning queries.
 */
@Entity(
    tableName = "peer_locations",
    indices = [
        Index(value = ["peerId"]),
        Index(value = ["timestamp"])
    ]
)
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Mesh node ID of the peer (e.g. "NODE-d48923d7"). */
    val peerId: String,

    /** Latitude in decimal degrees. */
    val lat: Double,

    /** Longitude in decimal degrees. */
    val lng: Double,

    /** GPS horizontal accuracy in metres. */
    val accuracyMeters: Float,

    /** Unix epoch millisecond timestamp of the fix. */
    val timestamp: Long,

    /** Hop count the packet traversed before arriving at this node. */
    val hopCount: Int
)
