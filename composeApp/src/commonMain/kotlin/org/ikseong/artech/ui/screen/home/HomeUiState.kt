package org.ikseong.artech.ui.screen.home

import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: ArticleCategory? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val hasMorePages: Boolean = true,
)
