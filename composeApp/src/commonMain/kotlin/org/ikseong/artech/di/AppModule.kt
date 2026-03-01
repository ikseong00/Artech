package org.ikseong.devnews.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.ikseong.devnews.data.local.AppDatabase
import org.ikseong.devnews.data.local.DataStoreFactory
import org.ikseong.devnews.data.local.DatabaseFactory
import org.ikseong.devnews.data.remote.SupabaseProvider
import org.ikseong.devnews.data.repository.ArticleRepository
import org.ikseong.devnews.data.repository.FavoriteRepository
import org.ikseong.devnews.data.repository.HistoryRepository
import org.ikseong.devnews.data.repository.SettingsRepository
import org.ikseong.devnews.ui.screen.detail.DetailViewModel
import org.ikseong.devnews.ui.screen.favorite.FavoriteViewModel
import org.ikseong.devnews.ui.screen.history.HistoryViewModel
import org.ikseong.devnews.ui.screen.home.HomeViewModel
import org.ikseong.devnews.ui.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseProvider.client }
    single { ArticleRepository(get()) }

    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().favoriteDao() }
    single { get<AppDatabase>().readHistoryDao() }

    single { FavoriteRepository(get()) }
    single { HistoryRepository(get()) }

    single { get<DataStoreFactory>().create() }
    single { SettingsRepository(get()) }
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoriteViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::SettingsViewModel)
}
