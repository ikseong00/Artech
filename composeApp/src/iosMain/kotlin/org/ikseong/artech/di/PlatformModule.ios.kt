package org.ikseong.artech.di

import org.ikseong.artech.analytics.AnalyticsTracker
import org.ikseong.artech.analytics.IosAnalyticsTracker
import org.ikseong.artech.data.local.DataStoreFactory
import org.ikseong.artech.data.local.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseFactory() }
    single { DataStoreFactory() }
    single<AnalyticsTracker> { IosAnalyticsTracker() }
}
