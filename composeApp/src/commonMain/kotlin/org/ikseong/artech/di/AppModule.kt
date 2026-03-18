package org.ikseong.artech.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.ikseong.artech.data.local.AppDatabase
import org.ikseong.artech.data.local.DataStoreFactory
import org.ikseong.artech.data.local.DatabaseFactory
import org.ikseong.artech.data.remote.SupabaseProvider
import org.ikseong.artech.data.repository.AppUpdateRepository
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.AuthRepository
import org.ikseong.artech.data.repository.FavoriteRepository
import org.ikseong.artech.data.repository.FeedbackRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.data.repository.SessionManager
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.ui.screen.blog.BlogViewModel
import org.ikseong.artech.ui.screen.bloglist.BlogListViewModel
import org.ikseong.artech.ui.screen.detail.DetailViewModel
import org.ikseong.artech.ui.screen.favorite.FavoriteViewModel
import org.ikseong.artech.ui.screen.history.HistoryViewModel
import org.ikseong.artech.ui.screen.home.HomeViewModel
import org.ikseong.artech.ui.screen.onboarding.OnboardingViewModel
import org.ikseong.artech.ui.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseProvider.client }
    single { ArticleRepository(get()) }
    single { AppUpdateRepository(get(), get()) }
    single { FeedbackRepository(get()) }
    single { SessionManager(get()) }
    single { AuthRepository(get(), get()) }

    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
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
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoriteViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::DetailViewModel)
    viewModelOf(::BlogViewModel)
    viewModelOf(::BlogListViewModel)
    viewModelOf(::SettingsViewModel)
}
