package com.example.zerogrid.map.domain

/**
 * Configuration that maps [MovementState] to adaptive GPS sampling parameters.
 * All durations are in milliseconds.
 */
data class SamplingConfig(
    /** Minimum time between GPS location requests. */
    val minIntervalMs: Long,
    /** Minimum distance change to trigger a location update. */
    val minDistanceMeters: Float,
    /** Maximum time between mesh broadcasts even if no movement. */
    val heartbeatIntervalMs: Long
)

object AdaptiveSamplingConfig {
    private val STATIONARY = SamplingConfig(
        minIntervalMs      = 60_000L,  // 1 min
        minDistanceMeters  = 15f,
        heartbeatIntervalMs = 300_000L // 5 min
    )
    private val WALKING = SamplingConfig(
        minIntervalMs      = 15_000L,  // 15 sec
        minDistanceMeters  = 30f,
        heartbeatIntervalMs = 120_000L // 2 min
    )
    private val VEHICLE = SamplingConfig(
        minIntervalMs      = 5_000L,   // 5 sec
        minDistanceMeters  = 100f,
        heartbeatIntervalMs = 120_000L // 2 min
    )
    private val UNKNOWN = SamplingConfig(
        minIntervalMs      = 20_000L,  // 20 sec
        minDistanceMeters  = 30f,
        heartbeatIntervalMs = 120_000L // 2 min
    )

    fun forState(state: MovementState): SamplingConfig = when (state) {
        MovementState.STATIONARY -> STATIONARY
        MovementState.WALKING    -> WALKING
        MovementState.VEHICLE    -> VEHICLE
        MovementState.UNKNOWN    -> UNKNOWN
    }
}
