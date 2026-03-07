package org.ikseong.artech.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.repository.FavoriteRepository

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState = combine(
        favoriteRepository.getAll(),
        selectedCategory,
    ) { allArticles, category ->
        val filtered = if (category != null) {
            allArticles.filter { it.category == category }
        } else {
            allArticles
        }
        val categories = allArticles
            .mapNotNull { it.category }
            .distinct()
            .sorted()
        FavoriteUiState(
            articles = filtered,
            allArticles = allArticles,
            selectedCategory = category,
            categories = categories,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoriteUiState(),
    )

    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            favoriteRepository.toggle(article)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            favoriteRepository.deleteAll()
        }
    }
}
