package org.ikseong.artech.ui.screen.latest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.navigation.Route
import kotlin.coroutines.cancellation.CancellationException

class LatestFeedViewModel(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.LatestFeed>()

    private val _uiState = MutableStateFlow(
        LatestFeedUiState(selectedCategory = route.initialCategory),
    )
    val uiState: StateFlow<LatestFeedUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<LatestFeedUiEffect>(capacity = Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val lastVisit = settingsRepository.lastVisitTime.first()
            _uiState.update { it.copy(lastVisitTime = lastVisit) }
            settingsRepository.updateLastVisitTime()
            launch { loadCategories() }
            launch {
                historyRepository.getReadArticleIds().collect { ids ->
                    _uiState.update { it.copy(readArticleIds = ids) }
                }
            }
            loadArticles()
        }
    }

    private suspend fun loadCategories() {
        try {
            val categories = articleRepository.getCategories()
            _uiState.update { it.copy(categories = categories) }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Exception) {
            // Keep the feed usable even if category metadata fails.
        }
    }

    fun loadArticles() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.startRefresh() }

        loadJob = viewModelScope.launch {
            try {
                val articles = fetchArticles(offset = 0)
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMorePages) return

        currentPage++
        _uiState.update { it.copy(isLoadingMore = true) }

        loadJob = viewModelScope.launch {
            try {
                val articles = fetchArticles(offset = currentPage * ArticleRepository.DEFAULT_PAGE_SIZE)
                _uiState.update {
                    it.copy(
                        articles = (it.articles + articles).distinctBy { article -> article.id },
                        isLoadingMore = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                currentPage--
                _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun selectCategory(category: String?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadArticles()
        _uiEffect.trySend(LatestFeedUiEffect.ScrollToTop)
    }

    fun toggleUnreadFilter() {
        _uiState.update { it.copy(showUnreadOnly = !it.showUnreadOnly) }
    }

    private suspend fun fetchArticles(offset: Int) =
        articleRepository.getArticles(
            category = _uiState.value.selectedCategory,
            offset = offset,
        )
}
