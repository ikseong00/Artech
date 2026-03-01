package org.ikseong.devnews.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.ikseong.devnews.data.local.dao.FavoriteDao
import org.ikseong.devnews.data.local.dao.ReadHistoryDao
import org.ikseong.devnews.data.local.entity.FavoriteEntity
import org.ikseong.devnews.data.local.entity.ReadHistoryEntity

@Database(
    entities = [FavoriteEntity::class, ReadHistoryEntity::class],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun readHistoryDao(): ReadHistoryDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
