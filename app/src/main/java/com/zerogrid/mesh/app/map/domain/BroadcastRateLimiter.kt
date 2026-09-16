package com.zerogrid.mesh.app.map.domain

import android.util.Log

/**
 * Token-bucket rate limiter preventing location broadcast flooding over the mesh.
 *
 * Rules:
 * - Max [maxTokens] broadcasts per [windowMs] sliding window.
 * - If the mesh routing queue depth exceeds [congestionThreshold], all broadcasts
 *   are suppressed regardless of available tokens.
 *
 * Thread-safe via @Synchronized.
 */
class BroadcastRateLimiter(
    private val maxTokens: Int = 4,
    private val windowMs: Long = 60_000L,
    private val congestionThreshold: Int = 30
) {
    companion object {
        private const val TAG = "BroadcastRateLimiter"
    }

    // Timestamps (ms) of recent broadcasts within the current window
    private val timestamps = ArrayDeque<Long>(maxTokens)

    /**
     * Returns true if a broadcast is permitted right now.
     *
     * @param currentQueueDepth Current routing engine relay queue depth.
     *                          Obtain via [MeshRoutingEngine.currentQueueDepth].
     */
    @Synchronized
    fun tryAcquire(currentQueueDepth: Int = 0): Boolean {
        if (currentQueueDepth >= congestionThreshold) {
            Log.d(TAG, "Broadcast suppressed: mesh congestion (queue=$currentQueueDepth)")
            return false
        }

        val now = System.currentTimeMillis()
        val windowStart = now - windowMs

        // Remove expired tokens outside the sliding window
        while (timestamps.isNotEmpty() && timestamps.first() < windowStart) {
            timestamps.removeFirst()
        }

        return if (timestamps.size < maxTokens) {
            timestamps.addLast(now)
            true
        } else {
            Log.d(TAG, "Broadcast suppressed: rate limit reached (${timestamps.size}/$maxTokens in last ${windowMs/1000}s)")
            false
        }
    }

    /** Resets the rate limiter (e.g. on service restart). */
    @Synchronized
    fun reset() {
        timestamps.clear()
    }
}
