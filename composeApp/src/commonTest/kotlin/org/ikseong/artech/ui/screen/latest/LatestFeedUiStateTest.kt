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

    @Test
    fun startRefresh_clears_loading_more_state() {
        val refreshing = LatestFeedUiState(
            isLoadingMore = true,
            hasMorePages = false,
            error = "failed",
        ).startRefresh()

        assertEquals(true, refreshing.isLoading)
        assertEquals(false, refreshing.isLoadingMore)
        assertEquals(true, refreshing.hasMorePages)
        assertEquals(null, refreshing.error)
    }

    @Test
    fun loadMoreSignal_changes_when_only_loaded_article_count_changes() {
        val before = LatestFeedUiState(
            articles = listOf(article(id = 1L), article(id = 2L)),
            showUnreadOnly = true,
            readArticleIds = setOf(1L, 2L),
        ).loadMoreSignal(shouldLoadMore = true)

        val after = LatestFeedUiState(
            articles = listOf(article(id = 1L), article(id = 2L), article(id = 3L)),
            showUnreadOnly = true,
            readArticleIds = setOf(1L, 2L, 3L),
        ).loadMoreSignal(shouldLoadMore = true)

        assertEquals(false, before == after)
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
