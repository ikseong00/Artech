package org.ikseong.artech.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(dataModule, viewModelModule, platformModule)
    }
}
