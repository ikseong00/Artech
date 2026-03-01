package org.ikseong.devnews.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    val id: Long,
    val title: String,
    val link: String,
    val summary: String? = null,
    val category: ArticleCategory? = null,
    @SerialName("blog_source")
    val blogSource: String,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)
