package org.ikseong.artech.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AnalyticsEventsTest {
    @Test
    fun articleOpenEventKeepsStableNameAndContext() {
        val event = AnalyticsEvents.articleOpen(
            articleId = 42,
            source = "home_today_picks",
            category = "android",
            blogSource = "android-developers",
        )

        assertEquals("article_open", event.name)
        assertEquals("42", event.parameters["article_id"])
        assertEquals("home_today_picks", event.parameters["source"])
        assertEquals("android", event.parameters["category"])
        assertEquals("android-developers", event.parameters["blog_source"])
    }

    @Test
    fun blankOptionalArticleContextIsOmitted() {
        val event = AnalyticsEvents.articleOpen(
            articleId = 7,
            source = "latest_feed",
            category = "",
            blogSource = null,
        )

        assertFalse("category" in event.parameters)
        assertFalse("blog_source" in event.parameters)
    }

    @Test
    fun categorySelectionEventUsesStableParameterNames() {
        val event = AnalyticsEvents.categorySelect(
            source = "home_topic_hub",
            category = "ios",
        )

        assertEquals("category_select", event.name)
        assertEquals("home_topic_hub", event.parameters["source"])
        assertEquals("ios", event.parameters["category"])
    }
}
