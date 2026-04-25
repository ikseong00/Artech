package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.HistoryArticle
import org.ikseong.artech.data.model.SavedFavoriteArticle

object HomeInterestProfileCalculator {

    private const val ReadWeight = 1.0
    private const val FavoriteWeight = 2.0
    private const val DayInMilliseconds = 24 * 60 * 60 * 1000L

    fun calculate(
        readHistory: List<HistoryArticle>,
        favorites: List<SavedFavoriteArticle>,
        now: Instant,
    ): HomeInterestProfile {
        val categoryScores = linkedMapOf<String, Double>()
        val blogScores = linkedMapOf<String, Double>()

        readHistory.forEach { historyArticle ->
            val score = ReadWeight * recencyMultiplier(historyArticle.readAt, now)
            addScore(categoryScores, historyArticle.article.category, score)
            addScore(blogScores, historyArticle.article.blogSource, score)
        }

        favorites.forEach { favoriteArticle ->
            val score = FavoriteWeight * recencyMultiplier(favoriteArticle.savedAt, now)
            addScore(categoryScores, favoriteArticle.article.category, score)
            addScore(blogScores, favoriteArticle.article.blogSource, score)
        }

        return HomeInterestProfile(
            categoryScores = categoryScores.toMap(),
            blogScores = blogScores.toMap(),
        )
    }

    private fun addScore(
        scores: MutableMap<String, Double>,
        key: String?,
        score: Double,
    ) {
        val normalizedKey = key?.trim().orEmpty()
        if (normalizedKey.isEmpty()) return
        scores[normalizedKey] = (scores[normalizedKey] ?: 0.0) + score
    }

    private fun recencyMultiplier(
        timestamp: Instant,
        now: Instant,
    ): Double {
        val ageInMilliseconds = (now.toEpochMilliseconds() - timestamp.toEpochMilliseconds()).coerceAtLeast(0L)
        return when {
            ageInMilliseconds <= 2 * DayInMilliseconds -> 1.0
            ageInMilliseconds <= 7 * DayInMilliseconds -> 0.75
            ageInMilliseconds <= 14 * DayInMilliseconds -> 0.5
            else -> 0.25
        }
    }
}
