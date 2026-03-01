package org.ikseong.devnews

import android.app.Application
import org.ikseong.devnews.di.dataModule
import org.ikseong.devnews.di.platformModule
import org.ikseong.devnews.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DevNewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DevNewsApplication)
            modules(dataModule, viewModelModule, platformModule)
        }
    }
}
