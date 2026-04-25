package org.ikseong.artech.ui.screen.home

import org.ikseong.artech.data.model.Article

data class InterestTopicShortcut(
    val category: String,
    val unreadCount: Int,
)

data class HomeFeedSections(
    val todayPicks: List<Article>,
    val interestTopics: List<InterestTopicShortcut>,
    val missedArticles: List<Article>,
    val latestPreview: List<Article>,
)
