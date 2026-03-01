package org.ikseong.devnews.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ikseong.devnews.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE articleId = :articleId)")
    fun isFavorite(articleId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE articleId = :articleId")
    suspend fun deleteByArticleId(articleId: Long)

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()
}
