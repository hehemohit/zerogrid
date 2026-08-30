package com.example.zerogrid.mesh.engine

import org.json.JSONObject
import java.util.UUID

/**
 * Data packet structure for ZeroGrid off-grid mesh transport and multi-hop routing.
 */
data class MeshPacket(
    val packetId: String = UUID.randomUUID().toString(),
    val senderId: String,
    val recipientId: String = BROADCAST_ADDRESS,
    var ttl: Int = DEFAULT_TTL,
    var hopCount: Int = 0,
    val type: PacketType,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val signature: String = ""
) {
    companion object {
        const val BROADCAST_ADDRESS = "*"
        const val DEFAULT_TTL = 5

        fun fromJson(jsonStr: String): MeshPacket? {
            return try {
                val json = JSONObject(jsonStr)
                val sender = json.optString("senderId", "")
                if (sender.isEmpty()) return parseJsonFallback(jsonStr)

                MeshPacket(
                    packetId = json.optString("packetId", UUID.randomUUID().toString()),
                    senderId = sender,
                    recipientId = json.optString("recipientId", BROADCAST_ADDRESS),
                    ttl = json.optInt("ttl", DEFAULT_TTL),
                    hopCount = json.optInt("hopCount", 0),
                    type = PacketType.valueOf(json.optString("type", PacketType.DIRECT_MESSAGE.name)),
                    payload = json.optString("payload", ""),
                    timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                    signature = json.optString("signature", "")
                )
            } catch (e: Exception) {
                parseJsonFallback(jsonStr)
            }
        }

        private fun parseJsonFallback(jsonStr: String): MeshPacket? {
            return try {
                fun extractString(key: String): String {
                    val regex = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
                    return regex.find(jsonStr)?.groupValues?.get(1) ?: ""
                }
                fun extractInt(key: String, default: Int): Int {
                    val regex = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
                    return regex.find(jsonStr)?.groupValues?.get(1)?.toIntOrNull() ?: default
                }
                fun extractLong(key: String, default: Long): Long {
                    val regex = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
                    return regex.find(jsonStr)?.groupValues?.get(1)?.toLongOrNull() ?: default
                }

                val sender = extractString("senderId")
                if (sender.isEmpty()) return null

                val packetId = extractString("packetId").ifEmpty { UUID.randomUUID().toString() }
                val recipientId = extractString("recipientId").ifEmpty { BROADCAST_ADDRESS }
                val typeStr = extractString("type").ifEmpty { PacketType.DIRECT_MESSAGE.name }
                val payload = extractString("payload")
                val signature = extractString("signature")
                val ttl = extractInt("ttl", DEFAULT_TTL)
                val hopCount = extractInt("hopCount", 0)
                val timestamp = extractLong("timestamp", System.currentTimeMillis())

                MeshPacket(
                    packetId = packetId,
                    senderId = sender,
                    recipientId = recipientId,
                    ttl = ttl,
                    hopCount = hopCount,
                    type = PacketType.valueOf(typeStr),
                    payload = payload,
                    timestamp = timestamp,
                    signature = signature
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toJson(): String {
        val safePayload = payload.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        return """{"packetId":"$packetId","senderId":"$senderId","recipientId":"$recipientId","ttl":$ttl,"hopCount":$hopCount,"type":"${type.name}","payload":"$safePayload","timestamp":$timestamp,"signature":"$signature"}"""
    }

    fun toByteArray(): ByteArray {
        return toJson().toByteArray(Charsets.UTF_8)
    }
}
