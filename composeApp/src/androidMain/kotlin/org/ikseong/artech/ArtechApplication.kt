package org.ikseong.artech

import android.app.Application
import org.ikseong.artech.di.dataModule
import org.ikseong.artech.di.platformModule
import org.ikseong.artech.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtechApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ArtechApplication)
            modules(dataModule, viewModelModule, platformModule)
        }
    }
}
