package org.ikseong.artech.analytics

data class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
)

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
}

object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) = Unit
}

object AnalyticsEvents {
    fun screenView(screenName: String): AnalyticsEvent =
        event(
            name = "screen_view",
            "screen_name" to screenName,
        )

    fun articleOpen(
        articleId: Long,
        source: String,
        category: String? = null,
        blogSource: String? = null,
    ): AnalyticsEvent =
        event(
            name = "article_open",
            "article_id" to articleId,
            "source" to source,
            "category" to category,
            "blog_source" to blogSource,
        )

    fun categorySelect(
        source: String,
        category: String?,
    ): AnalyticsEvent =
        event(
            name = "category_select",
            "source" to source,
            "category" to (category ?: "all"),
        )

    fun blogOpen(
        source: String,
        blogSource: String,
    ): AnalyticsEvent =
        event(
            name = "blog_open",
            "source" to source,
            "blog_source" to blogSource,
        )

    fun latestFeedOpen(source: String): AnalyticsEvent =
        event(
            name = "latest_feed_open",
            "source" to source,
        )

    fun recommendationRefresh(source: String): AnalyticsEvent =
        event(
            name = "recommendation_refresh",
            "source" to source,
        )

    fun unreadFilterToggle(
        source: String,
        enabled: Boolean,
    ): AnalyticsEvent =
        event(
            name = "unread_filter_toggle",
            "source" to source,
            "enabled" to enabled,
        )

    fun tabSelect(destination: String): AnalyticsEvent =
        event(
            name = "tab_select",
            "destination" to destination,
        )

    private fun event(
        name: String,
        vararg parameters: Pair<String, Any?>,
    ): AnalyticsEvent =
        AnalyticsEvent(
            name = name,
            parameters = parameters
                .mapNotNull { (key, value) ->
                    value?.toString()
                        ?.takeIf(String::isNotBlank)
                        ?.let { key to it }
                }
                .toMap(),
        )
}
