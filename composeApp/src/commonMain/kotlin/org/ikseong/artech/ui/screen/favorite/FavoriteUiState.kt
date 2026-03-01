package org.ikseong.devnews.ui.screen.favorite

import org.ikseong.devnews.data.model.Article

data class FavoriteUiState(
    val articles: List<Article> = emptyList(),
)
