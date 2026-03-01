package org.ikseong.devnews.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: String?,
    val blogSource: String,
    val displayDate: Long,
    val savedAt: Long,
)
