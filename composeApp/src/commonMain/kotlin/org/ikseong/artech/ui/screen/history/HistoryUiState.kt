package org.ikseong.artech.ui.screen.history

import org.ikseong.artech.data.model.HistoryArticle

data class HistoryUiState(
    val groupedArticles: List<HistoryGroup> = emptyList(),
) {
    val isEmpty: Boolean get() = groupedArticles.isEmpty()
}

data class HistoryGroup(
    val label: String,
    val articles: List<HistoryArticle>,
)
