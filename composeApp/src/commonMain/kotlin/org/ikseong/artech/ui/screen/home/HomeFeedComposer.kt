package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.CategoryGroup
import kotlin.math.abs

object HomeFeedComposer {

    private const val DayInMilliseconds = 24 * 60 * 60 * 1000L
    private const val TodayPickCount = 3
    private const val InterestTopicCount = 4
    private const val MissedArticleCount = 3
    private const val LatestPreviewCount = 4
    private const val CategoryWeight = 1.6
    private const val BlogWeight = 1.1

    fun compose(
        candidates: List<Article>,
        readArticleIds: Set<Long>,
        profile: HomeInterestProfile,
        now: Instant,
    ): HomeFeedSections {
        val recentCandidates = candidates.distinctBy { it.id }
        val unreadCandidates = recentCandidates.filter { it.id !in readArticleIds }
        val rankedUnread = unreadCandidates
            .map { article ->
                ScoredArticle(
                    article = article,
                    interestScore = interestScore(
                        article = article,
                        profile = profile,
                    ),
                    recentScore = recentScore(
                        article = article,
                        now = now,
                    ),
                )
            }
            .sortedWith(scoredArticleComparator())

        val todayPicks = pickTodayPicks(rankedUnread)
        val todayPickIds = todayPicks.map { it.id }.toSet()
        val interestTopics = buildInterestTopics(
            unreadCandidates = unreadCandidates,
            profile = profile,
        )
        val missedArticles = rankedUnread
            .asSequence()
            .filter { it.article.id !in todayPickIds }
            .map { it to missedArticleBonus(it.article, now) }
            .filter { (_, bonus) -> bonus > 0.0 }
            .sortedWith(
                compareByDescending<Pair<ScoredArticle, Double>> { it.first.interestScore + it.second }
                    .thenByDescending { it.first.article.displayDate.toEpochMilliseconds() }
                    .thenByDescending { it.first.article.id }
            )
            .map { it.first.article }
            .take(MissedArticleCount)
            .toList()
        val latestPreview = recentCandidates
            .sortedWith(
                compareByDescending<Article> { it.displayDate.toEpochMilliseconds() }
                    .thenByDescending { it.id }
            )
            .take(LatestPreviewCount)
        return HomeFeedSections(
            todayPicks = todayPicks,
            interestTopics = interestTopics,
            missedArticles = missedArticles,
            latestPreview = latestPreview,
        )
    }

    private fun pickTodayPicks(rankedUnread: List<ScoredArticle>): List<Article> {
        val categorySelections = mutableSetOf<String>()
        val blogSelections = mutableSetOf<String>()
        val selected = mutableListOf<Article>()

        rankedUnread.forEach { scored ->
            if (selected.size >= TodayPickCount) return@forEach
            val normalizedCategory = normalize(scored.article.category)
            val normalizedBlog = normalize(scored.article.blogSource)
            if (normalizedCategory in categorySelections || normalizedBlog in blogSelections) {
                return@forEach
            }
            selected += scored.article
            if (normalizedCategory.isNotEmpty()) categorySelections += normalizedCategory
            if (normalizedBlog.isNotEmpty()) blogSelections += normalizedBlog
        }

        rankedUnread.forEach { scored ->
            if (selected.size >= TodayPickCount) return@forEach
            if (selected.any { it.id == scored.article.id }) return@forEach
            selected += scored.article
        }

        return selected
    }

    private fun buildInterestTopics(
        unreadCandidates: List<Article>,
        profile: HomeInterestProfile,
    ): List<InterestTopicShortcut> {
        val unreadCountByDisplayCategory = unreadCandidates
            .mapNotNull { article -> article.category?.let(CategoryGroup::toDisplayName) }
            .groupingBy { it }
            .eachCount()

        return profile.topCategories
            .map(CategoryGroup::toDisplayName)
            .distinct()
            .mapNotNull { category ->
                val unreadCount = unreadCountByDisplayCategory[category] ?: 0
                if (unreadCount == 0) {
                    null
                } else {
                    InterestTopicShortcut(
                        category = category,
                        unreadCount = unreadCount,
                    )
                }
            }
            .take(InterestTopicCount)
    }

    private fun scoredArticleComparator() =
        compareByDescending<ScoredArticle> { it.interestScore + it.recentScore }
            .thenByDescending { it.recentScore }
            .thenByDescending { it.article.displayDate.toEpochMilliseconds() }
            .thenByDescending { it.article.id }

    private fun interestScore(
        article: Article,
        profile: HomeInterestProfile,
    ): Double = profile.scoreForCategory(article.category.orEmpty()) * CategoryWeight +
        profile.scoreForBlog(article.blogSource) * BlogWeight

    private fun recentScore(
        article: Article,
        now: Instant,
    ): Double {
        val ageInDays = ageInDays(article, now)
        return when {
            ageInDays < 1.0 -> 3.0
            ageInDays < 2.0 -> 2.0
            ageInDays < 4.0 -> 1.0
            ageInDays < 7.0 -> 0.5
            ageInDays < 14.0 -> 0.2
            else -> 0.0
        }
    }

    private fun missedArticleBonus(
        article: Article,
        now: Instant,
    ): Double {
        val ageInDays = ageInDays(article, now)
        if (ageInDays < 2.0 || ageInDays > 7.0) return 0.0
        val centeredBonus = 5.5 - abs(ageInDays - 5.0) * 1.2
        val stalePenalty = (ageInDays - 5.0).coerceAtLeast(0.0) * 1.0
        return (centeredBonus - stalePenalty).coerceAtLeast(0.1)
    }

    private fun ageInDays(
        article: Article,
        now: Instant,
    ): Double {
        val ageInMilliseconds = (now.toEpochMilliseconds() - article.displayDate.toEpochMilliseconds())
            .coerceAtLeast(0L)
        return ageInMilliseconds.toDouble() / DayInMilliseconds.toDouble()
    }

    private fun normalize(value: String?): String = value?.trim().orEmpty()

    private data class ScoredArticle(
        val article: Article,
        val interestScore: Double,
        val recentScore: Double,
    )
}
