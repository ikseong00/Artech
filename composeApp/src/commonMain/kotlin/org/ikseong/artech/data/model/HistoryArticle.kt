package org.ikseong.artech.data.model

import kotlinx.datetime.Instant

data class HistoryArticle(
    val article: Article,
    val readAt: Instant,
)
