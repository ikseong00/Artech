package org.ikseong.artech.di

import com.google.firebase.analytics.FirebaseAnalytics
import org.ikseong.artech.BuildKonfig
import org.ikseong.artech.analytics.AnalyticsTracker
import org.ikseong.artech.analytics.FirebaseAnalyticsTracker
import org.ikseong.artech.analytics.NoOpAnalyticsTracker
import org.ikseong.artech.data.local.DataStoreFactory
import org.ikseong.artech.data.local.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseFactory(get()) }
    single { DataStoreFactory(get()) }
    single { FirebaseAnalytics.getInstance(get()) }
    single<AnalyticsTracker> {
        if (BuildKonfig.FIREBASE_ANALYTICS_ENABLED.toBoolean()) {
            FirebaseAnalyticsTracker(get())
        } else {
            NoOpAnalyticsTracker
        }
    }
}
