package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

data class HomeUiState(
    val todayPicks: List<Article> = emptyList(),
    val interestTopics: List<InterestTopicShortcut> = emptyList(),
    val missedArticles: List<Article> = emptyList(),
    val latestPreview: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshingTodayPicks: Boolean = false,
    val error: String? = null,
    val lastVisitTime: Instant? = null,
)

sealed interface HomeUiEffect {
    data object ScrollToTop : HomeUiEffect
    data object ScrollRecommendedToStart : HomeUiEffect
}
