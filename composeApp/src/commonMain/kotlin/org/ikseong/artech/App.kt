package org.ikseong.devnews

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.devnews.data.model.ThemeMode
import org.ikseong.devnews.data.repository.SettingsRepository
import org.ikseong.devnews.navigation.AppNavigation
import org.ikseong.devnews.ui.theme.DevNewsTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val settingsRepository = koinInject<SettingsRepository>()
    val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    DevNewsTheme(darkTheme = darkTheme) {
        AppNavigation()
    }
}
