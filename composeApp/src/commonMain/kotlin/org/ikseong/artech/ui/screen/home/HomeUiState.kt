package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

data class HomeUiState(
    val randomBannerArticle: Article? = null,
    val todayPicks: List<Article> = emptyList(),
    val interestCategoryRecommendations: List<InterestCategoryRecommendation> = emptyList(),
    val interestTopics: List<InterestTopicShortcut> = emptyList(),
    val interestTopicUnreadTotal: Int = 0,
    val availableCategories: List<String> = emptyList(),
    val selectedInterestCategories: List<String> = emptyList(),
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
