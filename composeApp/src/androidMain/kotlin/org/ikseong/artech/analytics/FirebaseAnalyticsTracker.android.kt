package org.ikseong.artech.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name, event.parameters.toBundle())
    }
}

private fun Map<String, String>.toBundle(): Bundle =
    Bundle().also { bundle ->
        forEach { (key, value) ->
            bundle.putString(key, value)
        }
    }
