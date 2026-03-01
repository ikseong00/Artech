package org.ikseong.devnews.ui.screen.history

import org.ikseong.devnews.data.model.HistoryArticle

data class HistoryUiState(
    val groupedArticles: List<HistoryGroup> = emptyList(),
) {
    val isEmpty: Boolean get() = groupedArticles.isEmpty()
}

data class HistoryGroup(
    val label: String,
    val articles: List<HistoryArticle>,
)
