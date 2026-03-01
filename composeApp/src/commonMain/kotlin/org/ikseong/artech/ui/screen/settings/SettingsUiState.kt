package org.ikseong.devnews.ui.screen.settings

import org.ikseong.devnews.data.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appVersion: String = "1.0",
)
