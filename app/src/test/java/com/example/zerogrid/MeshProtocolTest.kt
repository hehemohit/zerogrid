package com.example.zerogrid

import com.example.zerogrid.mesh.engine.DeduplicationCache
import com.example.zerogrid.mesh.engine.MeshPacket
import com.example.zerogrid.mesh.engine.MeshRoutingEngine
import com.example.zerogrid.mesh.engine.PacketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshProtocolTest {

    @Test
    fun testPacketSerialization() {
        val packet = MeshPacket(
            senderId = "NODE-ALICE",
            recipientId = "NODE-BOB",
            ttl = 5,
            type = PacketType.DIRECT_MESSAGE,
            payload = "Hello Mesh!"
        )

        val json = packet.toJson()
        val deserialized = MeshPacket.fromJson(json)

        assertNotNull(deserialized)
        assertEquals(packet.packetId, deserialized?.packetId)
        assertEquals("NODE-ALICE", deserialized?.senderId)
        assertEquals("NODE-BOB", deserialized?.recipientId)
        assertEquals(5, deserialized?.ttl)
        assertEquals(PacketType.DIRECT_MESSAGE, deserialized?.type)
        assertEquals("Hello Mesh!", deserialized?.payload)
    }

    @Test
    fun testDeduplicationCacheLoopPrevention() {
        val cache = DeduplicationCache()
        val packetId = "PACKET-123"

        // First time processing packet -> Should record and return false (not duplicate)
        val firstResult = cache.isDuplicateAndRecord(packetId)
        assertFalse(firstResult)

        // Second time processing same packet -> Should detect duplicate and return true
        val secondResult = cache.isDuplicateAndRecord(packetId)
        assertTrue(secondResult)
    }

    @Test
    fun testRoutingEngineDuplicateDrop() {
        val routingEngine = MeshRoutingEngine(localNodeId = "NODE-CHARLIE")
        val packet = MeshPacket(
            senderId = "NODE-ALICE",
            recipientId = "NODE-CHARLIE",
            type = PacketType.DIRECT_MESSAGE,
            payload = "Test Message"
        )

        // Process first time
        routingEngine.processInboundPacket(packet)

        // Process duplicate second time -> Should be caught by deduplication cache
        routingEngine.processInboundPacket(packet)
    }
}
