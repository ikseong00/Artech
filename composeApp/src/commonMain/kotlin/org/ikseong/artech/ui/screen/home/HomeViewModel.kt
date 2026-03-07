package org.ikseong.artech.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import org.ikseong.artech.data.repository.SettingsRepository
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(capacity = Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val lastVisit = settingsRepository.lastVisitTime.first()
            _uiState.update { it.copy(lastVisitTime = lastVisit) }
            settingsRepository.updateLastVisitTime()
            launch { loadCategories() }
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
            // 카테고리 로딩 실패 시 빈 목록 유지
        }
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
                        articles = (it.articles + articles).distinctBy { a -> a.id },
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

    fun selectCategory(category: String?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadArticles()
        _uiEffect.trySend(HomeUiEffect.ScrollToTop)
    }

    private suspend fun fetchArticles(offset: Int) =
        articleRepository.getArticles(
            category = _uiState.value.selectedCategory,
            offset = offset,
        )
}
