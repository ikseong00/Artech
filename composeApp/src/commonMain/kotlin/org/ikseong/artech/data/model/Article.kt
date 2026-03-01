package org.ikseong.devnews.data.model

import kotlinx.datetime.Instant

data class Article(
    val id: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: ArticleCategory?,
    val blogSource: String,
    val displayDate: Instant,
)
