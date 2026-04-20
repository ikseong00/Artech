package org.ikseong.artech

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.AppVersionInfo
import org.ikseong.artech.data.model.ThemeMode
import org.ikseong.artech.data.model.UpdateType
import org.ikseong.artech.data.repository.AppUpdateRepository
import org.ikseong.artech.data.repository.AuthRepository
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.navigation.AppNavigation
import org.ikseong.artech.ui.component.ForceUpdateDialog
import org.ikseong.artech.ui.component.OptionalUpdateDialog
import org.ikseong.artech.ui.screen.onboarding.OnboardingScreen
import org.ikseong.artech.ui.theme.ArtechTheme
import org.ikseong.artech.util.SystemBarsThemeEffect
import org.ikseong.artech.util.openUrl
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

    val appUpdateRepository = koinInject<AppUpdateRepository>()
    var updateType by remember { mutableStateOf(UpdateType.NONE) }
    var versionInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

    LaunchedEffect(Unit) {
        val (type, info) = appUpdateRepository.checkForUpdate()
        updateType = type
        versionInfo = info
    }

    val scope = rememberCoroutineScope()

    ArtechTheme(darkTheme = darkTheme) {
        SystemBarsThemeEffect(darkTheme = darkTheme)

        when (isLoggedIn) {
            null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            false -> OnboardingScreen()
            true -> AppNavigation()
        }

        when (updateType) {
            UpdateType.FORCE -> {
                versionInfo?.let { info ->
                    ForceUpdateDialog(
                        onUpdate = { openUrl(info.storeUrl) },
                    )
                }
            }
            UpdateType.OPTIONAL -> {
                versionInfo?.let { info ->
                    OptionalUpdateDialog(
                        onUpdate = { openUrl(info.storeUrl) },
                        onDismiss = {
                            scope.launch {
                                settingsRepository.setSkippedOptionalVersion(info.optionalUpdateVersion)
                            }
                            updateType = UpdateType.NONE
                        },
                    )
                }
            }
            UpdateType.NONE -> {}
        }
    }
}
