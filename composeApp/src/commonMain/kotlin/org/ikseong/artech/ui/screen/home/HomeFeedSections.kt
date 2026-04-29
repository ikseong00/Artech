package org.ikseong.artech.ui.screen.home

import org.ikseong.artech.data.model.Article

data class InterestTopicShortcut(
    val category: String,
    val unreadCount: Int,
)

data class InterestCategoryRecommendation(
    val category: String,
    val articles: List<Article>,
)

data class HomeFeedSections(
    val randomBannerArticle: Article?,
    val todayPicks: List<Article>,
    val interestCategoryRecommendations: List<InterestCategoryRecommendation>,
    val interestTopics: List<InterestTopicShortcut>,
    val interestTopicUnreadTotal: Int,
    val missedArticles: List<Article>,
    val latestPreview: List<Article>,
)
