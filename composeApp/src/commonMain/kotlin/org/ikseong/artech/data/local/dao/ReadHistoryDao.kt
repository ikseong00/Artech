package org.ikseong.devnews.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ikseong.devnews.data.local.entity.ReadHistoryEntity

@Dao
interface ReadHistoryDao {

    @Query("SELECT * FROM read_history ORDER BY readAt DESC")
    fun getAll(): Flow<List<ReadHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReadHistoryEntity)

    @Query("DELETE FROM read_history")
    suspend fun deleteAll()
}
