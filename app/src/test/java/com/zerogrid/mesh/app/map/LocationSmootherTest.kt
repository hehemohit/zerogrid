package com.zerogrid.mesh.app.map

import com.zerogrid.mesh.app.map.domain.LocationSmoother
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [LocationSmoother] EMA filtering and accuracy gating.
 * Tests pure JVM math independent of Android framework stubs.
 */
class LocationSmootherTest {

    private lateinit var smoother: LocationSmoother

    @Before
    fun setUp() {
        smoother = LocationSmoother(alpha = 0.3f, maxAccuracyMeters = 50f)
    }

    @Test
    fun `first fix initializes filter and is returned as-is`() {
        val result = smoother.filterRaw(28.6139, 77.2090, 10f)
        assertNotNull(result)
        assertEquals(28.6139, result!!.first, 0.0001)
        assertEquals(77.2090, result.second, 0.0001)
    }

    @Test
    fun `fix with accuracy above threshold is discarded`() {
        val result = smoother.filterRaw(28.6139, 77.2090, 51f)
        assertNull("Fix with accuracy > 50m must be discarded", result)
    }

    @Test
    fun `ema smoothing reduces jitter for stationary device`() {
        // Feed stationary fixes with small noise
        repeat(10) { i ->
            smoother.filterRaw(28.6139 + i * 0.000_001, 77.2090, 5f)
        }
        val result = smoother.filterRaw(28.6139, 77.2090, 10f)!!

        // EMA should smooth the drift; result should be very close to 28.6139
        assertTrue(
            "EMA lat should be close to true position (within 0.001 deg)",
            Math.abs(result.first - 28.6139) < 0.001
        )
    }

    @Test
    fun `reset clears filter state and re-initializes on next fix`() {
        smoother.filterRaw(0.0, 0.0, 10f)
        smoother.reset()
        val result = smoother.filterRaw(48.8566, 2.3522, 10f) // Paris
        assertNotNull(result)
        assertEquals(48.8566, result!!.first, 0.0001)
        assertEquals(2.3522, result.second, 0.0001)
    }
}
