package org.ikseong.artech.analytics

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IosAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) {
        IosAnalyticsBridge.logEvent(
            name = event.name,
            parametersJson = Json.encodeToString(event.parameters),
        )
    }
}

object IosAnalyticsBridge {
    private var logger: ((name: String, parametersJson: String) -> Unit)? = null

    fun setLogger(logger: (name: String, parametersJson: String) -> Unit) {
        this.logger = logger
    }

    fun logEvent(
        name: String,
        parametersJson: String,
    ) {
        logger?.invoke(name, parametersJson)
    }
}
