package org.ikseong.devnews.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.devnews.data.model.ThemeMode
import org.ikseong.devnews.data.repository.FavoriteRepository
import org.ikseong.devnews.data.repository.HistoryRepository
import org.ikseong.devnews.data.repository.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val uiState = settingsRepository.themeMode
        .map { SettingsUiState(themeMode = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            favoriteRepository.deleteAll()
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            historyRepository.deleteAll()
        }
    }
}
