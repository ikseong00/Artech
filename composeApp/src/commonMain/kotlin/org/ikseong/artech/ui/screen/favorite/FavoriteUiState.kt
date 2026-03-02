package org.ikseong.artech.ui.screen.favorite

import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory

data class FavoriteUiState(
    val articles: List<Article> = emptyList(),
    val allArticles: List<Article> = emptyList(),
    val selectedCategories: Set<ArticleCategory> = emptySet(),
)
