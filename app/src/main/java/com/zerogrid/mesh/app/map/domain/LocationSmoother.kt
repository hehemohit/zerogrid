package com.zerogrid.mesh.app.map.domain

import android.location.Location

/**
 * Exponential Moving Average (EMA) filter for GPS coordinates.
 * Suppresses GPS drift noise for stationary devices and small jitter.
 *
 * alpha = 0.3 means new samples contribute 30% weight to the running average.
 * Fixes with accuracy > MAX_ACCURACY_METERS are discarded without updating the filter.
 */
class LocationSmoother(
    private val alpha: Float = 0.3f,
    private val maxAccuracyMeters: Float = 50f
) {
    private var smoothedLat: Double? = null
    private var smoothedLng: Double? = null

    /**
     * Core math for EMA filtering and accuracy gating.
     * Pure JVM function independent of Android framework classes.
     */
    fun filterRaw(lat: Double, lng: Double, accuracy: Float): Pair<Double, Double>? {
        if (accuracy > maxAccuracyMeters) return null

        smoothedLat = if (smoothedLat == null) {
            lat
        } else {
            alpha * lat + (1 - alpha) * smoothedLat!!
        }
        smoothedLng = if (smoothedLng == null) {
            lng
        } else {
            alpha * lng + (1 - alpha) * smoothedLng!!
        }

        return Pair(smoothedLat!!, smoothedLng!!)
    }

    /**
     * Feeds a new [Location] fix through the EMA filter.
     * @return A new [Location] with smoothed coordinates, or null if the fix is too inaccurate.
     */
    fun filter(location: Location): Location? {
        val (lat, lng) = filterRaw(location.latitude, location.longitude, location.accuracy) ?: return null
        return Location(location).apply {
            latitude  = lat
            longitude = lng
            // Keep original accuracy, time, and altitude
        }
    }

    /** Resets the filter state (e.g. after extended inactivity). */
    fun reset() {
        smoothedLat = null
        smoothedLng = null
    }
}
