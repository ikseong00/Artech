package org.ikseong.devnews.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.devnews.data.model.Article
import org.ikseong.devnews.data.repository.ArticleRepository
import org.ikseong.devnews.data.repository.FavoriteRepository
import org.ikseong.devnews.data.repository.HistoryRepository
import org.ikseong.devnews.navigation.Route

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val detail = savedStateHandle.toRoute<Route.Detail>()
    val link: String = detail.link

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoriteRepository.isFavorite(detail.articleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val article = articleRepository.getArticle(detail.articleId)
            _article.value = article
            if (article != null) {
                historyRepository.record(article)
            }
        }
    }

    fun toggleFavorite() {
        val article = _article.value ?: return
        viewModelScope.launch {
            favoriteRepository.toggle(article)
        }
    }
}
