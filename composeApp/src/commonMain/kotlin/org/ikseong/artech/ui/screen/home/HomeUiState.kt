package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val recommendedArticles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: ArticleCategory? = null,
    val hasMorePages: Boolean = true,
    val lastVisitTime: Instant? = null,
)

sealed interface HomeUiEffect {
    data object ScrollToTop : HomeUiEffect
}
