package org.ikseong.artech.ui.screen.settings

import org.ikseong.artech.data.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appVersion: String = "1.0",
)
