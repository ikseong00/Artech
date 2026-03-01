package org.ikseong.artech.ui.screen.favorite

import org.ikseong.artech.data.model.Article

data class FavoriteUiState(
    val articles: List<Article> = emptyList(),
)
