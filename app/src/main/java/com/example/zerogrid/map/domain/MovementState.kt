package com.example.zerogrid.map.domain

/**
 * Movement state of a mesh node, determined by the on-device [MovementClassifier].
 * Used to drive adaptive GPS sampling rates without Google Play Services.
 *
 * @param id Binary identifier stored in the location packet wire format.
 */
enum class MovementState(val id: Byte) {
    /** Device has been stationary for the last sampling window. */
    STATIONARY(0),
    /** Device is moving at walking speed (accelerometer variance in walking band). */
    WALKING(1),
    /** Device is moving at vehicle speed (high accelerometer variance). */
    VEHICLE(2),
    /** Classifier has insufficient data to determine state. */
    UNKNOWN(3);

    companion object {
        fun fromId(id: Byte): MovementState =
            entries.firstOrNull { it.id == id } ?: UNKNOWN
    }
}
