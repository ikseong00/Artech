package org.ikseong.artech.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.ikseong.artech.data.local.AppDatabase
import org.ikseong.artech.data.local.DataStoreFactory
import org.ikseong.artech.data.local.DatabaseFactory
import org.ikseong.artech.data.remote.SupabaseProvider
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.FavoriteRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.ui.screen.detail.DetailViewModel
import org.ikseong.artech.ui.screen.favorite.FavoriteViewModel
import org.ikseong.artech.ui.screen.history.HistoryViewModel
import org.ikseong.artech.ui.screen.home.HomeViewModel
import org.ikseong.artech.ui.screen.settings.SettingsViewModel
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
