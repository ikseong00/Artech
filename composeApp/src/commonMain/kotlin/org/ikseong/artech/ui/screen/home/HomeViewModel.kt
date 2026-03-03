package org.ikseong.artech.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.ArticleCategory
import org.ikseong.artech.data.repository.ArticleRepository
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(capacity = Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val searchQueryFlow = MutableStateFlow("")

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        loadArticles()
        observeSearchQuery()
    }

    fun loadArticles() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.copy(isLoading = true, error = null, hasMorePages = true) }

        loadJob = viewModelScope.launch {
            try {
                val articles = fetchArticles(offset = 0)
                val recommended = if (articles.size >= 5) {
                    articles.shuffled().take(5)
                } else {
                    articles.shuffled()
                }
                _uiState.update {
                    it.copy(
                        articles = articles,
                        recommendedArticles = recommended,
                        isLoading = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
                _uiEffect.trySend(HomeUiEffect.ScrollToTop)
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMorePages) return

        currentPage++
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            try {
                val articles = fetchArticles(offset = currentPage * ArticleRepository.DEFAULT_PAGE_SIZE)
                _uiState.update {
                    it.copy(
                        articles = it.articles + articles,
                        isLoadingMore = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                currentPage--
                _uiState.update {
                    it.copy(isLoadingMore = false, error = e.message)
                }
            }
        }
    }

    fun selectCategory(category: ArticleCategory?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        reloadCurrentView()
    }

    private fun reloadCurrentView() {
        val state = _uiState.value
        if (state.isSearchActive && state.searchQuery.isNotBlank()) {
            searchArticles(state.searchQuery)
        } else {
            loadArticles()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun toggleSearch() {
        val isActive = !_uiState.value.isSearchActive
        _uiState.update {
            it.copy(
                isSearchActive = isActive,
                searchQuery = if (!isActive) "" else it.searchQuery,
            )
        }
        if (!isActive) {
            loadArticles()
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .drop(1)
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        loadArticles()
                    } else {
                        searchArticles(query)
                    }
                }
        }
    }

    private fun searchArticles(query: String) {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.copy(isLoading = true, error = null) }

        loadJob = viewModelScope.launch {
            try {
                val articles = articleRepository.searchArticles(
                    keyword = query,
                    category = _uiState.value.selectedCategory,
                )
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        hasMorePages = false,
                    )
                }
                _uiEffect.trySend(HomeUiEffect.ScrollToTop)
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private suspend fun fetchArticles(offset: Int) =
        articleRepository.getArticles(
            category = _uiState.value.selectedCategory,
            offset = offset,
        )
}
