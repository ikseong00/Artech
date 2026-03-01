package org.ikseong.artech

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.artech.data.model.ThemeMode
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.navigation.AppNavigation
import org.ikseong.artech.ui.theme.ArtechTheme
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

    ArtechTheme(darkTheme = darkTheme) {
        AppNavigation()
    }
}
