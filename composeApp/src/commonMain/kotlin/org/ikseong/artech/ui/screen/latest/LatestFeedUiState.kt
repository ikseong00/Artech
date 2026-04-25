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

    internal fun startRefresh(): LatestFeedUiState = copy(
        isLoading = true,
        isLoadingMore = false,
        error = null,
        hasMorePages = true,
    )

    internal fun loadMoreSignal(shouldLoadMore: Boolean): LatestFeedLoadMoreSignal =
        LatestFeedLoadMoreSignal(
            shouldLoadMore = shouldLoadMore,
            loadedArticleCount = articles.size,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            hasMorePages = hasMorePages,
            showUnreadOnly = showUnreadOnly,
        )
}

sealed interface LatestFeedUiEffect {
    data object ScrollToTop : LatestFeedUiEffect
}

internal data class LatestFeedLoadMoreSignal(
    val shouldLoadMore: Boolean,
    val loadedArticleCount: Int,
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
    val hasMorePages: Boolean,
    val showUnreadOnly: Boolean,
)
