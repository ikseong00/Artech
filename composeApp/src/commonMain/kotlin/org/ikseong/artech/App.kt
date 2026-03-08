package org.ikseong.artech

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.artech.data.model.ThemeMode
import org.ikseong.artech.data.repository.AuthRepository
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.navigation.AppNavigation
import org.ikseong.artech.ui.screen.onboarding.OnboardingScreen
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

    val authRepository = koinInject<AuthRepository>()
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    ArtechTheme(darkTheme = darkTheme) {
        when (isLoggedIn) {
            null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            false -> OnboardingScreen()
            true -> AppNavigation()
        }
    }
}
