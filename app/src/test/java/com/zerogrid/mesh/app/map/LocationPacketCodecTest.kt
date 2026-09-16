package com.zerogrid.mesh.app.map

import com.zerogrid.mesh.app.map.data.LocationPacket
import com.zerogrid.mesh.app.map.data.LocationPacketCodec
import com.zerogrid.mesh.app.map.domain.MovementState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [LocationPacketCodec] binary serialization round-trip.
 * Tests run on JVM — no Android framework dependency.
 */
class LocationPacketCodecTest {

    private fun makePacket(
        lat: Double = 28.6139,
        lng: Double = 77.2090,
        accuracy: Float = 10.5f,
        battery: Int = 72,
        ttl: Int = 3,
        state: MovementState = MovementState.WALKING,
        timestamp: Long = System.currentTimeMillis()
    ) = LocationPacket(
        peerId         = "NODE-testpeer",
        peerIdHash     = LocationPacketCodec.computePeerIdHash("NODE-testpeer"),
        lat            = lat,
        lng            = lng,
        accuracyMeters = accuracy,
        timestamp      = timestamp,
        batteryPercent = battery,
        ttl            = ttl,
        movementState  = state
    )

    @Test
    fun `round-trip encode then decode returns equivalent packet`() {
        val original = makePacket()
        val encoded = LocationPacketCodec.encode(original)
        assertNotNull("encode must not return null", encoded)

        val decoded = LocationPacketCodec.decode("NODE-testpeer", encoded!!)
        assertNotNull("decode must not return null", decoded)

        assertEquals(original.lat, decoded!!.lat, 0.000_02) // ~2m tolerance for fixed-point
        assertEquals(original.lng, decoded.lng, 0.000_02)
        assertEquals(original.batteryPercent, decoded.batteryPercent)
        assertEquals(original.movementState, decoded.movementState)
        assertEquals(original.ttl, decoded.ttl)
    }

    @Test
    fun `fixed-point latitude precision within 11cm`() {
        val precisionDeg = 0.000_001 // 1e-6 degrees ~ 0.111 m
        val original = makePacket(lat = 51.507_351, lng = -0.127_758) // London
        val encoded  = LocationPacketCodec.encode(original)!!
        val decoded  = LocationPacketCodec.decode("NODE-testpeer", encoded)!!

        assertTrue(
            "Lat precision must be within 11cm (1e-6 degrees)",
            Math.abs(original.lat - decoded.lat) <= precisionDeg
        )
        assertTrue(
            "Lng precision must be within 11cm",
            Math.abs(original.lng - decoded.lng) <= precisionDeg
        )
    }

    @Test
    fun `delta timestamp encodes and decodes correctly`() {
        val ts = LocationPacket.EPOCH_BASE + 86_400_000L * 500 // 500 days after epoch base
        val original = makePacket(timestamp = ts)
        val encoded  = LocationPacketCodec.encode(original)!!
        val decoded  = LocationPacketCodec.decode("NODE-testpeer", encoded)!!

        assertEquals("Timestamp must survive round-trip", ts, decoded.timestamp)
    }

    @Test
    fun `truncated byte array returns null without exception`() {
        val encoded = LocationPacketCodec.encode(makePacket())!!
        val truncated = encoded.copyOf(10) // Intentionally too short
        val result = LocationPacketCodec.decode("NODE-testpeer", truncated)
        assertNull("Truncated packet must return null", result)
    }

    @Test
    fun `unknown version byte returns null`() {
        val encoded = LocationPacketCodec.encode(makePacket())!!.copyOf()
        encoded[0] = 99.toByte() // Overwrite version byte
        val result = LocationPacketCodec.decode("NODE-testpeer", encoded)
        assertNull("Unknown version must return null", result)
    }

    @Test
    fun `battery -1 encodes and decodes as -1`() {
        val original = makePacket(battery = -1)
        val decoded  = LocationPacketCodec.decode("NODE-testpeer", LocationPacketCodec.encode(original)!!)!!
        assertEquals(-1, decoded.batteryPercent)
    }

    @Test
    fun `all movement states round-trip correctly`() {
        MovementState.entries.forEach { state ->
            val original = makePacket(state = state)
            val decoded  = LocationPacketCodec.decode("NODE-testpeer", LocationPacketCodec.encode(original)!!)!!
            assertEquals("MovementState $state must survive round-trip", state, decoded.movementState)
        }
    }
}
