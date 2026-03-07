package org.ikseong.artech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_history")
data class ReadHistoryEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: String?,
    val blogSource: String,
    val publishedAt: Long?,
    val createdAt: Long?,
    val readAt: Long,
    val thumbnailUrl: String? = null,
)
