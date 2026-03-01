package org.ikseong.devnews.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.devnews.data.repository.FavoriteRepository

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    val uiState = favoriteRepository.getAll()
        .map { FavoriteUiState(articles = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoriteUiState(),
        )

    fun deleteAll() {
        viewModelScope.launch {
            favoriteRepository.deleteAll()
        }
    }
}
