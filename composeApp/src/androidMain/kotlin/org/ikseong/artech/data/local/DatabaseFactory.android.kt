package org.ikseong.artech.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<AppDatabase> {
        migrateLegacyDbIfNeeded()
        val dbFile = context.getDatabasePath(DB_NAME)
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
        )
    }

    private fun migrateLegacyDbIfNeeded() {
        val newDb = context.getDatabasePath(DB_NAME)
        val legacyDb = context.getDatabasePath(LEGACY_DB_NAME)
        if (newDb.exists() || !legacyDb.exists()) return

        legacyDb.renameTo(newDb)
        context.getDatabasePath("$LEGACY_DB_NAME-wal").let { if (it.exists()) it.renameTo(context.getDatabasePath("$DB_NAME-wal")) }
        context.getDatabasePath("$LEGACY_DB_NAME-shm").let { if (it.exists()) it.renameTo(context.getDatabasePath("$DB_NAME-shm")) }
    }

    companion object {
        private const val DB_NAME = "artech.db"
        private const val LEGACY_DB_NAME = "devnews.db"
    }
}
