package org.ikseong.artech.ui.screen.blog

import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.BlogMeta

data class BlogUiState(
    val blogMeta: BlogMeta? = null,
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val hasMorePages: Boolean = true,
    val showUnreadOnly: Boolean = false,
    val readArticleIds: Set<Long> = emptySet(),
    val totalArticleCount: Int = 0,
    val dateRange: Pair<String, String>? = null,
) {
    val displayArticles: List<Article>
        get() = if (showUnreadOnly) articles.filter { it.id !in readArticleIds } else articles
}

sealed interface BlogUiEffect {
    data object ScrollToTop : BlogUiEffect
}
