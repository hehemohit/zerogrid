package com.example.zerogrid.map.data

import com.example.zerogrid.map.domain.MovementState

/**
 * Represents a location update broadcast over the ZeroGrid mesh network.
 *
 * Designed to be serialized into a compact binary format by [LocationPacketCodec]
 * for efficient transmission over BLE and Wi-Fi Direct transports.
 *
 * @param peerId            Full mesh node ID of the peer (e.g. "NODE-d48923d7").
 * @param peerIdHash        16-byte truncated SHA-256 hash of peerId for privacy-preserving indexing.
 * @param lat               Latitude in decimal degrees.
 * @param lng               Longitude in decimal degrees.
 * @param accuracyMeters    GPS horizontal accuracy radius in meters.
 * @param timestamp         Unix epoch millisecond timestamp of the fix.
 * @param batteryPercent    Node device battery level (0-100).
 * @param ttl               Remaining hop TTL (capped to 3-4 for location pings).
 * @param movementState     Classifier-determined movement state of the peer device.
 * @param signature         Truncated HMAC-SHA256 signature for replay/spoofing protection.
 */
data class LocationPacket(
    val peerId: String,
    val peerIdHash: ByteArray = ByteArray(16),
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryPercent: Int = -1,
    val ttl: Int = LOCATION_DEFAULT_TTL,
    val movementState: MovementState = MovementState.UNKNOWN,
    val signature: ByteArray = ByteArray(0)
) {
    companion object {
        /** Default TTL for location pings - intentionally low for local proximity only. */
        const val LOCATION_DEFAULT_TTL = 3
        /** Shared epoch base for delta timestamp encoding (2024-01-01T00:00:00Z). */
        const val EPOCH_BASE = 1704067200000L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocationPacket) return false
        return peerId == other.peerId &&
            peerIdHash.contentEquals(other.peerIdHash) &&
            lat == other.lat &&
            lng == other.lng &&
            accuracyMeters == other.accuracyMeters &&
            timestamp == other.timestamp &&
            batteryPercent == other.batteryPercent &&
            ttl == other.ttl &&
            movementState == other.movementState &&
            signature.contentEquals(other.signature)
    }

    override fun hashCode(): Int {
        var result = peerId.hashCode()
        result = 31 * result + peerIdHash.contentHashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + accuracyMeters.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + batteryPercent
        result = 31 * result + ttl
        result = 31 * result + movementState.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}
