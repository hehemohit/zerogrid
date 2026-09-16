package com.example.zerogrid.map

import com.example.zerogrid.map.domain.BroadcastRateLimiter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BroadcastRateLimiter] token-bucket and congestion suppression logic.
 */
class BroadcastRateLimiterTest {

    private lateinit var limiter: BroadcastRateLimiter

    @Before
    fun setUp() {
        limiter = BroadcastRateLimiter(maxTokens = 4, windowMs = 60_000L, congestionThreshold = 30)
    }

    @Test
    fun `permits up to maxTokens broadcasts in window`() {
        repeat(4) {
            assertTrue("Token $it should be permitted", limiter.tryAcquire(0))
        }
    }

    @Test
    fun `blocks broadcast after maxTokens exceeded`() {
        repeat(4) { limiter.tryAcquire(0) }
        assertFalse("5th broadcast in window must be blocked", limiter.tryAcquire(0))
    }

    @Test
    fun `congestion above threshold suppresses all broadcasts`() {
        assertFalse("Broadcast during congestion must be blocked", limiter.tryAcquire(31))
    }

    @Test
    fun `congestion at threshold minus 1 does not suppress`() {
        assertTrue("Broadcast just below congestion threshold must be allowed", limiter.tryAcquire(29))
    }

    @Test
    fun `reset clears all tokens and allows fresh broadcasts`() {
        repeat(4) { limiter.tryAcquire(0) }
        assertFalse("Must be blocked after 4 acquires", limiter.tryAcquire(0))
        limiter.reset()
        assertTrue("Must be permitted after reset", limiter.tryAcquire(0))
    }
}
