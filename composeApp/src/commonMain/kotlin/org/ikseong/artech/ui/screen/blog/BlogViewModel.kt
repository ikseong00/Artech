package org.ikseong.artech.ui.screen.blog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.BlogMetaRegistry
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.navigation.Route
import kotlin.coroutines.cancellation.CancellationException

class BlogViewModel(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val blog = savedStateHandle.toRoute<Route.Blog>()
    val blogSource: String = blog.blogSource

    private val _uiState = MutableStateFlow(BlogUiState())
    val uiState: StateFlow<BlogUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<BlogUiEffect>(capacity = Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        _uiState.update { it.copy(blogMeta = BlogMetaRegistry.getBlogMeta(blogSource)) }
        viewModelScope.launch {
            launch { loadCategories() }
            launch { loadBlogStats() }
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
            val categories = articleRepository.getCategoriesByBlog(blogSource)
            _uiState.update { it.copy(categories = categories) }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Exception) {
            // 카테고리 로딩 실패 시 빈 목록 유지
        }
    }

    private suspend fun loadBlogStats() {
        try {
            val stats = articleRepository.getBlogStats(blogSource)
            _uiState.update {
                it.copy(
                    totalArticleCount = stats.totalCount,
                    dateRange = if (stats.earliestDate != null && stats.latestDate != null) {
                        stats.earliestDate to stats.latestDate
                    } else {
                        null
                    },
                )
            }
        } catch (_: CancellationException) {
            throw CancellationException()
        } catch (_: Exception) {
            // 통계 로딩 실패 시 기본값 유지
        }
    }

    fun loadArticles() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.copy(isLoading = true, error = null, hasMorePages = true) }

        loadJob = viewModelScope.launch {
            try {
                val articles = fetchArticles(offset = 0)
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
                        articles = (it.articles + articles).distinctBy { a -> a.id },
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
        _uiEffect.trySend(BlogUiEffect.ScrollToTop)
    }

    fun toggleUnreadFilter() {
        _uiState.update { it.copy(showUnreadOnly = !it.showUnreadOnly) }
    }

    private suspend fun fetchArticles(offset: Int) =
        articleRepository.getArticlesByBlog(
            blogSource = blogSource,
            category = _uiState.value.selectedCategory,
            offset = offset,
        )
}
