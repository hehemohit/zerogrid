package com.example.zerogrid.map.data

import android.util.Log
import com.example.zerogrid.map.domain.MovementState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/**
 * Binary serializer / deserializer for [LocationPacket] optimized for BLE MTU constraints.
 *
 * Wire format (37 bytes fixed-header + variable signature):
 * [1B version][1B movementState_id][16B peerIdHash][4B lat_fixed_point][4B lng_fixed_point]
 * [2B accuracy_cm][6B timestamp_delta_ms][1B battery][1B ttl][Nb signature]
 *
 * Fixed-point coordinates: lat/lng multiplied by 1_000_000 for ~11 cm precision.
 * Delta timestamp: milliseconds since EPOCH_BASE stored in 6 bytes (big-endian long, lower 6 bytes).
 * Accuracy: stored as centimetres in a signed short (max 32767cm = 327m).
 */
object LocationPacketCodec {

    private const val TAG = "LocationPacketCodec"
    const val VERSION: Byte = 1
    private const val FIXED_HEADER_SIZE = 36 // bytes before signature

    // Coordinate precision: 1e6 gives ~0.111 m precision
    private const val COORD_SCALE = 1_000_000.0

    /**
     * Encodes a [LocationPacket] to a [ByteArray].
     * Returns null if the packet cannot be safely encoded.
     */
    fun encode(packet: LocationPacket): ByteArray? {
        return try {
            val sigLen = packet.signature.size.coerceAtMost(32)
            val buf = ByteBuffer.allocate(FIXED_HEADER_SIZE + sigLen).order(ByteOrder.BIG_ENDIAN)

            buf.put(VERSION)
            buf.put(packet.movementState.id)

            // 16-byte peerIdHash: compute from peerId if not pre-filled
            val hash = if (packet.peerIdHash.any { it != 0.toByte() }) {
                packet.peerIdHash
            } else {
                computePeerIdHash(packet.peerId)
            }
            buf.put(hash, 0, 16)

            // Fixed-point coordinates
            buf.putInt((packet.lat * COORD_SCALE).toInt())
            buf.putInt((packet.lng * COORD_SCALE).toInt())

            // Accuracy in centimetres (short)
            val accuracyCm = (packet.accuracyMeters * 100.0).toInt().coerceIn(0, Short.MAX_VALUE.toInt())
            buf.putShort(accuracyCm.toShort())

            // 6-byte delta timestamp
            val delta = (packet.timestamp - LocationPacket.EPOCH_BASE).coerceAtLeast(0L)
            val deltaBytes = ByteArray(8)
            ByteBuffer.wrap(deltaBytes).order(ByteOrder.BIG_ENDIAN).putLong(delta)
            buf.put(deltaBytes, 2, 6) // Skip top 2 bytes — fits in 6 bytes until year ~2178

            // Battery (0-100, -1 stored as 0xFF)
            val battByte = if (packet.batteryPercent < 0) 0xFF.toByte() else packet.batteryPercent.toByte()
            buf.put(battByte)

            // TTL
            buf.put(packet.ttl.toByte())

            // Signature
            if (sigLen > 0) buf.put(packet.signature, 0, sigLen)

            buf.array()
        } catch (e: Exception) {
            Log.e(TAG, "Encode failed", e)
            null
        }
    }

    /**
     * Decodes a [ByteArray] into a [LocationPacket].
     * Returns null if the data is malformed, truncated, or has an unknown version.
     */
    fun decode(peerId: String, bytes: ByteArray): LocationPacket? {
        if (bytes.size < FIXED_HEADER_SIZE) {
            Log.w(TAG, "Packet too short: ${bytes.size} < $FIXED_HEADER_SIZE")
            return null
        }
        return try {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

            val version = buf.get()
            if (version != VERSION) {
                Log.w(TAG, "Unknown location packet version: $version (expected $VERSION)")
                return null
            }

            val movementId = buf.get()
            val movementState = MovementState.fromId(movementId)

            val peerIdHash = ByteArray(16)
            buf.get(peerIdHash)

            val latFixed = buf.getInt()
            val lngFixed = buf.getInt()
            val lat = latFixed / COORD_SCALE
            val lng = lngFixed / COORD_SCALE

            val accuracyCm = buf.getShort().toInt() and 0xFFFF
            val accuracyMeters = accuracyCm / 100.0f

            val deltaBytes = ByteArray(8)
            buf.get(deltaBytes, 2, 6) // Read 6 bytes into positions 2-7
            val delta = ByteBuffer.wrap(deltaBytes).order(ByteOrder.BIG_ENDIAN).getLong()
            val timestamp = LocationPacket.EPOCH_BASE + delta

            val battRaw = buf.get().toInt() and 0xFF
            val batteryPercent = if (battRaw == 0xFF) -1 else battRaw

            val ttl = buf.get().toInt() and 0xFF

            val sigLen = bytes.size - FIXED_HEADER_SIZE
            val signature = if (sigLen > 0) {
                ByteArray(sigLen).also { buf.get(it) }
            } else ByteArray(0)

            LocationPacket(
                peerId = peerId,
                peerIdHash = peerIdHash,
                lat = lat,
                lng = lng,
                accuracyMeters = accuracyMeters,
                timestamp = timestamp,
                batteryPercent = batteryPercent,
                ttl = ttl,
                movementState = movementState,
                signature = signature
            )
        } catch (e: Exception) {
            Log.e(TAG, "Decode failed for peerId=$peerId", e)
            null
        }
    }

    /** Computes a 16-byte truncated SHA-256 hash of the peer ID. */
    fun computePeerIdHash(peerId: String): ByteArray {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.digest(peerId.toByteArray(Charsets.UTF_8)).copyOf(16)
        } catch (e: Exception) {
            ByteArray(16)
        }
    }
}
