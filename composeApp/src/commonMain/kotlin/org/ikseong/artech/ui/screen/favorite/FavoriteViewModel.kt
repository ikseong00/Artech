package org.ikseong.artech.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory
import org.ikseong.artech.data.repository.FavoriteRepository

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val selectedCategories = MutableStateFlow<Set<ArticleCategory>>(emptySet())

    val uiState = combine(
        favoriteRepository.getAll(),
        selectedCategories,
    ) { allArticles, categories ->
        val filtered = if (categories.isNotEmpty()) {
            allArticles.filter { it.category in categories }
        } else {
            allArticles
        }
        FavoriteUiState(
            articles = filtered,
            allArticles = allArticles,
            selectedCategories = categories,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoriteUiState(),
    )

    fun toggleCategory(category: ArticleCategory) {
        selectedCategories.value = selectedCategories.value.toMutableSet().apply {
            if (category in this) remove(category) else add(category)
        }
    }

    fun clearCategoryFilter() {
        selectedCategories.value = emptySet()
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
