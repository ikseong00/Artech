package org.ikseong.devnews.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<AppDatabase> {
        val dbFile = context.getDatabasePath(DB_NAME)
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
        )
    }

    companion object {
        private const val DB_NAME = "devnews.db"
    }
}
