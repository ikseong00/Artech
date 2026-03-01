package org.ikseong.devnews.data.model

import kotlinx.datetime.Instant

data class HistoryArticle(
    val article: Article,
    val readAt: Instant,
)
