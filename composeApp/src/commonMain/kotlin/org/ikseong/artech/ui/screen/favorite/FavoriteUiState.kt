package org.ikseong.artech.ui.screen.favorite

import org.ikseong.artech.data.model.Article

data class FavoriteUiState(
    val articles: List<Article> = emptyList(),
    val allArticles: List<Article> = emptyList(),
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
)
