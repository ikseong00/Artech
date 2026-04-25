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

        assertEquals(listOf(1L, 3L, 5L), sections.todayPicks.map { it.id })
        assertEquals("Android", sections.interestTopics.first().category)
        assertEquals(3, sections.interestTopics.first().unreadCount)
        assertEquals(listOf(6L, 4L, 8L), sections.missedArticles.map { it.id })
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
        assertEquals(emptyList(), sections.interestTopics)
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
