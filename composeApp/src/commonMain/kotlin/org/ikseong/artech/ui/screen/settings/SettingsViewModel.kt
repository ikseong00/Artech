package org.ikseong.artech.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.ThemeMode
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.FavoriteRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.data.repository.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val articleRepository: ArticleRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val availableCategories = MutableStateFlow<List<String>>(emptyList())

    val uiState = combine(
        settingsRepository.themeMode,
        settingsRepository.interestCategories,
        availableCategories,
    ) { themeMode, interestCategories, availableCategories ->
        SettingsUiState(
            themeMode = themeMode,
            availableCategories = availableCategories,
            interestCategories = interestCategories,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            availableCategories.value = runCatching {
                articleRepository.getCategories().sorted()
            }.getOrDefault(emptyList())
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun toggleInterestCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.toggleInterestCategory(category)
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
