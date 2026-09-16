package com.zerogrid.mesh.app.map.domain

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * On-device movement state classifier using [Sensor.TYPE_LINEAR_ACCELERATION].
 * Operates without Google Play Services by computing rolling variance over a
 * 5-second sliding window of accelerometer samples.
 *
 * Variance thresholds (empirically tuned):
 * - < 0.05 m/s^2^2  => STATIONARY
 * - < 1.5  m/s^2^2  => WALKING
 * - >= 1.5 m/s^2^2  => VEHICLE
 */
class MovementClassifier(context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "MovementClassifier"
        private const val WINDOW_SIZE_MS = 5_000L      // 5-second rolling window
        private const val STATIONARY_THRESHOLD = 0.05f // m/s^2 variance
        private const val WALKING_THRESHOLD = 1.5f     // m/s^2 variance
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val linearAccelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    // Ring buffer: pairs of (timestamp_ms, magnitude)
    private val samples = ArrayDeque<Pair<Long, Float>>(256)

    private val _movementState = MutableStateFlow(MovementState.UNKNOWN)
    val movementState: StateFlow<MovementState> = _movementState.asStateFlow()

    /**
     * Starts listening to the linear accelerometer.
     * Call from foreground service onCreate.
     */
    fun start() {
        if (linearAccelSensor == null) {
            Log.w(TAG, "Linear acceleration sensor unavailable — defaulting to UNKNOWN")
            return
        }
        sensorManager?.registerListener(this, linearAccelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG, "MovementClassifier started")
    }

    /** Stops the accelerometer listener. Call from foreground service onDestroy. */
    fun stop() {
        sensorManager?.unregisterListener(this)
        samples.clear()
        Log.d(TAG, "MovementClassifier stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        val now = System.currentTimeMillis()

        samples.addLast(now to magnitude)

        // Evict samples older than the rolling window
        val cutoff = now - WINDOW_SIZE_MS
        while (samples.isNotEmpty() && samples.first().first < cutoff) {
            samples.removeFirst()
        }

        if (samples.size < 5) return // Insufficient data

        // Compute variance
        val magnitudes = samples.map { it.second }
        val mean = magnitudes.average().toFloat()
        val variance = magnitudes.fold(0f) { acc, v -> acc + (v - mean) * (v - mean) } / magnitudes.size

        val newState = when {
            variance < STATIONARY_THRESHOLD -> MovementState.STATIONARY
            variance < WALKING_THRESHOLD    -> MovementState.WALKING
            else                            -> MovementState.VEHICLE
        }

        if (newState != _movementState.value) {
            Log.d(TAG, "Movement state changed: ${_movementState.value} -> $newState (variance=$variance)")
            _movementState.value = newState
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) { /* no-op */ }
}
