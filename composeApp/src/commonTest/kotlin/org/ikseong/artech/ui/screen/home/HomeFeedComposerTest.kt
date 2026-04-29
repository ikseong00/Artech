package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.ikseong.artech.data.model.Article

class HomeFeedComposerTest {

    @Test
    fun compose_builds_diversified_today_picks_missed_articles_and_recent_preview() {
        val now = Instant.parse("2026-04-25T12:00:00Z")
        val profile = HomeInterestProfile(
            categoryScores = mapOf(
                "Android" to 3.0,
                "AI" to 2.5,
                "Backend" to 1.75,
                "Web" to 1.0,
            ),
            blogScores = mapOf(
                "Android Weekly" to 2.5,
                "OpenAI" to 2.0,
                "Ktor" to 1.5,
                "Frontend Focus" to 0.75,
            ),
        )

        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(
                    id = 1L,
                    category = "Android",
                    blogSource = "Android Weekly",
                    publishedAt = "2026-04-25T10:00:00Z",
                ),
                article(
                    id = 2L,
                    category = "Android",
                    blogSource = "Android Weekly",
                    publishedAt = "2026-04-25T09:00:00Z",
                ),
                article(
                    id = 3L,
                    category = "AI",
                    blogSource = "OpenAI",
                    publishedAt = "2026-04-24T18:00:00Z",
                ),
                article(
                    id = 4L,
                    category = "AI",
                    blogSource = "OpenAI",
                    publishedAt = "2026-04-22T12:00:00Z",
                ),
                article(
                    id = 5L,
                    category = "Backend",
                    blogSource = "Ktor",
                    publishedAt = "2026-04-24T08:00:00Z",
                ),
                article(
                    id = 6L,
                    category = "Android",
                    blogSource = "Mobile Dev",
                    publishedAt = "2026-04-20T12:00:00Z",
                ),
                article(
                    id = 7L,
                    category = "Web",
                    blogSource = "Frontend Focus",
                    publishedAt = "2026-04-19T12:00:00Z",
                ),
                article(
                    id = 8L,
                    category = "Android",
                    blogSource = "Android Weekly",
                    publishedAt = "2026-04-18T12:00:00Z",
                ),
            ),
            readArticleIds = setOf(2L),
            profile = profile,
            now = now,
        )

        assertEquals(listOf(1L, 3L, 5L, 7L, 8L), sections.todayPicks.map { it.id })
        assertTrue(sections.randomBannerArticle?.id in listOf(1L, 3L, 4L, 5L, 6L, 7L, 8L))
        assertEquals("Android", sections.interestTopics.first().category)
        assertEquals(3, sections.interestTopics.first().unreadCount)
        assertEquals(7, sections.interestTopicUnreadTotal)
        assertEquals(listOf(6L, 4L), sections.missedArticles.map { it.id })
        assertFalse(sections.missedArticles.any { it.id in sections.todayPicks.map { article -> article.id } })
        assertEquals(listOf(1L, 2L, 3L, 5L), sections.latestPreview.map { it.id })
    }

    @Test
    fun compose_keeps_today_picks_available_when_interest_profile_is_empty() {
        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(
                    id = 1L,
                    category = "Android",
                    blogSource = "Android Weekly",
                    publishedAt = "2026-04-25T10:00:00Z",
                ),
                article(
                    id = 2L,
                    category = "AI",
                    blogSource = "OpenAI",
                    publishedAt = "2026-04-25T09:00:00Z",
                ),
            ),
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(
                categoryScores = emptyMap(),
                blogScores = emptyMap(),
            ),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertTrue(sections.todayPicks.isNotEmpty())
        assertEquals(listOf(1L, 2L), sections.todayPicks.map { it.id })
        assertEquals(listOf("Android", "AI"), sections.interestTopics.map { it.category })
        assertEquals(2, sections.interestTopicUnreadTotal)
    }

    @Test
    fun compose_builds_interest_category_recommendations_from_selected_categories() {
        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(1L, "Android", "Android Weekly", "2026-04-25T10:00:00Z"),
                article(2L, "Android", "Android Weekly", "2026-04-25T09:00:00Z"),
                article(3L, "AI", "OpenAI", "2026-04-25T08:00:00Z"),
                article(4L, "Backend", "Ktor", "2026-04-25T07:00:00Z"),
                article(5L, "AI", "OpenAI", "2026-04-24T10:00:00Z"),
                article(6L, "Android", "Mobile Dev", "2026-04-23T10:00:00Z"),
            ),
            readArticleIds = setOf(2L),
            profile = HomeInterestProfile(
                categoryScores = emptyMap(),
                blogScores = emptyMap(),
            ),
            selectedInterestCategories = listOf("Android", "AI"),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        val recommendationsByCategory = sections.interestCategoryRecommendations.associateBy { it.category }

        assertEquals(setOf("Android", "AI"), recommendationsByCategory.keys)
        assertTrue(recommendationsByCategory.getValue("Android").articles.all { it.category == "Android" })
        assertTrue(recommendationsByCategory.getValue("AI").articles.all { it.category == "AI" })
        assertFalse(recommendationsByCategory.values.flattenArticles().any { it.id == 2L })
        assertTrue(recommendationsByCategory.values.all { it.articles.size in 1..6 })
    }

    @Test
    fun compose_keeps_interest_category_recommendations_in_selected_order() {
        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(1L, "Android", "Android Weekly", "2026-04-25T10:00:00Z"),
                article(2L, "AI", "OpenAI", "2026-04-25T09:00:00Z"),
                article(3L, "Back-End", "Backend Blog", "2026-04-25T08:00:00Z"),
                article(4L, "Android", "Android Weekly", "2026-04-25T07:00:00Z"),
            ),
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(
                categoryScores = emptyMap(),
                blogScores = emptyMap(),
            ),
            selectedInterestCategories = listOf("AI", "Android", "Back-End"),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertEquals(
            listOf("AI", "Android", "Back-End"),
            sections.interestCategoryRecommendations.map { it.category },
        )
    }

    @Test
    fun composeHomeSections_skips_seen_today_picks_without_removing_latest_preview() {
        val candidates = listOf(
            article(1L, "Android", "Android Weekly", "2026-04-25T10:00:00Z"),
            article(2L, "AI", "OpenAI", "2026-04-25T09:00:00Z"),
            article(3L, "Backend", "Ktor", "2026-04-25T08:00:00Z"),
            article(4L, "Web", "Frontend Focus", "2026-04-25T07:00:00Z"),
        )

        val sections = composeHomeSections(
            feedComposer = HomeFeedComposer,
            candidates = candidates,
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(
                categoryScores = emptyMap(),
                blogScores = emptyMap(),
            ),
            seenTodayPickIds = setOf(1L, 2L, 3L),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertEquals(listOf(4L), sections.todayPicks.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), sections.latestPreview.map { it.id })
    }

    @Test
    fun interestTopics_use_display_category_for_grouped_categories() {
        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(1L, "QA", "QA Blog", "2026-04-25T10:00:00Z"),
                article(2L, "Automation", "Automation Blog", "2026-04-25T09:00:00Z"),
            ),
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(
                categoryScores = mapOf("QA" to 3.0),
                blogScores = emptyMap(),
            ),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertEquals("QA/Automation", sections.interestTopics.first().category)
        assertEquals(2, sections.interestTopics.first().unreadCount)
        assertEquals(2, sections.interestTopicUnreadTotal)
    }

    @Test
    fun interestTopicUnreadTotal_counts_all_unread_topics_beyond_visible_shortcuts() {
        val sections = HomeFeedComposer.compose(
            candidates = listOf(
                article(1L, "Android", "Android Weekly", "2026-04-25T10:00:00Z"),
                article(2L, "AI", "OpenAI", "2026-04-25T09:00:00Z"),
                article(3L, "Back-End", "Backend Blog", "2026-04-25T08:00:00Z"),
                article(4L, "Front-End", "Frontend Blog", "2026-04-25T07:00:00Z"),
                article(5L, "Infra", "Infra Blog", "2026-04-25T06:00:00Z"),
                article(6L, "iOS", "iOS Blog", "2026-04-25T05:00:00Z"),
            ),
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(
                categoryScores = emptyMap(),
                blogScores = emptyMap(),
            ),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertEquals(5, sections.interestTopics.size)
        assertEquals(6, sections.interestTopicUnreadTotal)
    }

    private fun Collection<InterestCategoryRecommendation>.flattenArticles(): List<Article> =
        flatMap { it.articles }

    private fun article(
        id: Long,
        category: String,
        blogSource: String,
        publishedAt: String,
    ): Article = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = category,
        blogSource = blogSource,
        publishedAt = Instant.parse(publishedAt),
        createdAt = Instant.parse(publishedAt),
    )
}
