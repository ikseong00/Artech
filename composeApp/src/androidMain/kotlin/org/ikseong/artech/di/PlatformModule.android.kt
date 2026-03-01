package org.ikseong.devnews.di

import org.ikseong.devnews.data.local.DataStoreFactory
import org.ikseong.devnews.data.local.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseFactory(get()) }
    single { DataStoreFactory(get()) }
}
