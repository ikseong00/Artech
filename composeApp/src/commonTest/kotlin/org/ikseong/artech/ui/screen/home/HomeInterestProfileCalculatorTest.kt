package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.HistoryArticle
import org.ikseong.artech.data.model.SavedFavoriteArticle

class HomeInterestProfileCalculatorTest {

    @Test
    fun favorite_weight_makes_ai_the_top_category_and_openai_top_blog() {
        val now = Instant.parse("2026-04-25T12:00:00Z")
        val firstRead = HistoryArticle(
            article = article(
                id = 1L,
                category = "Mobile",
                blogSource = "Kakao",
            ),
            readAt = Instant.parse("2026-04-24T10:00:00Z"),
        )
        val secondRead = HistoryArticle(
            article = article(
                id = 2L,
                category = "AI",
                blogSource = "Kakao",
            ),
            readAt = Instant.parse("2026-04-18T09:00:00Z"),
        )
        val favorite = SavedFavoriteArticle(
            article = article(
                id = 3L,
                category = "AI",
                blogSource = "OpenAI",
            ),
            savedAt = Instant.parse("2026-04-25T08:00:00Z"),
        )

        val profile = HomeInterestProfileCalculator.calculate(
            readHistory = listOf(firstRead, secondRead),
            favorites = listOf(favorite),
            now = now,
        )

        assertEquals(listOf("AI", "Mobile"), profile.topCategories)
        assertTrue(profile.scoreForCategory("AI") > profile.scoreForCategory("Mobile"))
        assertTrue(profile.scoreForBlog("OpenAI") > profile.scoreForBlog("Kakao"))
    }

    private fun article(
        id: Long,
        category: String,
        blogSource: String,
    ): Article = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = category,
        blogSource = blogSource,
        publishedAt = Instant.parse("2026-04-20T12:00:00Z"),
        createdAt = Instant.parse("2026-04-20T11:00:00Z"),
    )
}
