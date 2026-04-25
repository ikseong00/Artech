package org.ikseong.artech.ui.screen.latest

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import org.ikseong.artech.data.model.Article

class LatestFeedUiStateTest {

    @Test
    fun displayArticles_hides_read_ids_only_when_showUnreadOnly_is_true() {
        val articles = listOf(
            article(id = 1L),
            article(id = 2L),
            article(id = 3L),
        )

        assertEquals(
            listOf(1L, 2L, 3L),
            LatestFeedUiState(
                articles = articles,
                showUnreadOnly = false,
                readArticleIds = setOf(2L),
            ).displayArticles.map { it.id },
        )
        assertEquals(
            listOf(1L, 3L),
            LatestFeedUiState(
                articles = articles,
                showUnreadOnly = true,
                readArticleIds = setOf(2L),
            ).displayArticles.map { it.id },
        )
    }

    private fun article(id: Long): Article = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = "Android",
        blogSource = "Example",
        publishedAt = Instant.parse("2026-04-25T00:00:00Z"),
        createdAt = Instant.parse("2026-04-25T00:00:00Z"),
    )
}
