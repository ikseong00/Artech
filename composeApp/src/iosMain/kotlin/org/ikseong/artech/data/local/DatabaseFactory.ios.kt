package org.ikseong.devnews.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<AppDatabase> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbFilePath = requireNotNull(documentDirectory?.path) + "/$DB_NAME"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        )
    }

    companion object {
        private const val DB_NAME = "devnews.db"
    }
}
