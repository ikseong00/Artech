package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.repository.SettingsRepository

data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val recommendedArticles: List<Article> = emptyList(),
    val recommendRefreshRemaining: Int = SettingsRepository.MAX_RECOMMEND_REFRESHES,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val hasMorePages: Boolean = true,
    val lastVisitTime: Instant? = null,
    val showUnreadOnly: Boolean = false,
    val readArticleIds: Set<Long> = emptySet(),
) {
    val displayArticles: List<Article>
        get() = if (showUnreadOnly) {
            articles.filter { it.id !in readArticleIds }
        } else {
            articles
        }
}

sealed interface HomeUiEffect {
    data object ScrollToTop : HomeUiEffect
    data object ScrollRecommendedToStart : HomeUiEffect
}
