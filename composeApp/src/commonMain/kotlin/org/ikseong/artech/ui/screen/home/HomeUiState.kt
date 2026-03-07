package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val recommendedArticles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val hasMorePages: Boolean = true,
    val lastVisitTime: Instant? = null,
)

sealed interface HomeUiEffect {
    data object ScrollToTop : HomeUiEffect
}
