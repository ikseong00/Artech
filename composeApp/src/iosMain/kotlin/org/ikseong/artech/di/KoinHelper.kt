package org.ikseong.devnews.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(dataModule, viewModelModule, platformModule)
    }
}
