package com.example.zerogrid.map.domain

import android.location.Location

/**
 * Detects whether a new GPS fix represents a significant real-world movement
 * worth broadcasting over the mesh, filtering out GPS drift and noise.
 *
 * Uses [LocationSmoother] to pre-filter jitter and [AdaptiveSamplingConfig]
 * to apply state-dependent distance and interval thresholds.
 */
class SignificantMovementDetector(
    private val smoother: LocationSmoother = LocationSmoother()
) {
    private var lastBroadcastLocation: Location? = null
    private var lastBroadcastTimestamp: Long = 0L

    /**
     * Evaluates whether the given [location] fix should trigger a mesh broadcast.
     *
     * @param location      Raw GPS fix from [android.location.LocationManager].
     * @param movementState Current state from [MovementClassifier].
     * @return              Smoothed [Location] to broadcast, or null if not significant.
     */
    fun evaluate(location: Location, movementState: MovementState): Location? {
        val smoothed = smoother.filter(location) ?: return null
        val config = AdaptiveSamplingConfig.forState(movementState)
        val now = System.currentTimeMillis()

        val last = lastBroadcastLocation
        val timeSinceLast = now - lastBroadcastTimestamp

        // Heartbeat: always broadcast if we have not sent for the full heartbeat interval
        if (timeSinceLast >= config.heartbeatIntervalMs) {
            update(smoothed, now)
            return smoothed
        }

        // Too soon since last broadcast
        if (timeSinceLast < config.minIntervalMs) return null

        // Only broadcast if moved far enough
        if (last != null && smoothed.distanceTo(last) < config.minDistanceMeters) return null

        update(smoothed, now)
        return smoothed
    }

    private fun update(location: Location, timestamp: Long) {
        lastBroadcastLocation  = location
        lastBroadcastTimestamp = timestamp
    }

    /** Resets state after the service restarts. */
    fun reset() {
        lastBroadcastLocation  = null
        lastBroadcastTimestamp = 0L
        smoother.reset()
    }
}
