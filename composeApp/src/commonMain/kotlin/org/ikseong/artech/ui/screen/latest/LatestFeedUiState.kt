package org.ikseong.artech.ui.screen.latest

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

data class LatestFeedUiState(
    val articles: List<Article> = emptyList(),
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

sealed interface LatestFeedUiEffect {
    data object ScrollToTop : LatestFeedUiEffect
}
