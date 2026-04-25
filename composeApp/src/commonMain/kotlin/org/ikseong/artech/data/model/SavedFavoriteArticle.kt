package org.ikseong.artech.data.model

import kotlinx.datetime.Instant

data class SavedFavoriteArticle(
    val article: Article,
    val savedAt: Instant,
)
