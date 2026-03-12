package org.ikseong.artech.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.FeedbackReason
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.FavoriteRepository
import org.ikseong.artech.data.repository.FeedbackRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.navigation.Route

sealed interface FeedbackState {
    data object Idle : FeedbackState
    data object Submitting : FeedbackState
    data object Success : FeedbackState
    data class Error(val message: String) : FeedbackState
}

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
    private val feedbackRepository: FeedbackRepository,
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

    private var readTimerJob: Job? = null
    private var isMarkedAsRead = false

    private fun loadArticle() {
        viewModelScope.launch {
            val article = articleRepository.getArticle(detail.articleId)
            _article.value = article
        }
    }

    fun onUserScrolled() {
        if (readTimerJob != null || isMarkedAsRead) return
        readTimerJob = viewModelScope.launch {
            delay(20_000L)
            val article = _article.value ?: return@launch
            historyRepository.record(article)
            isMarkedAsRead = true
        }
    }

    private val _feedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Idle)
    val feedbackState: StateFlow<FeedbackState> = _feedbackState.asStateFlow()

    fun toggleFavorite() {
        val article = _article.value ?: return
        viewModelScope.launch {
            favoriteRepository.toggle(article)
        }
    }

    fun submitFeedback(reason: FeedbackReason, description: String? = null) {
        val articleId = _article.value?.id
        if (articleId == null) {
            _feedbackState.value = FeedbackState.Error("아티클을 찾을 수 없습니다")
            return
        }
        if (_feedbackState.value == FeedbackState.Submitting) return
        _feedbackState.value = FeedbackState.Submitting
        viewModelScope.launch {
            try {
                feedbackRepository.submitFeedback(articleId, reason, description)
                _feedbackState.value = FeedbackState.Success
            } catch (e: Exception) {
                _feedbackState.value = FeedbackState.Error(e.message ?: "피드백 전송에 실패했습니다")
            }
        }
    }

    fun resetFeedbackState() {
        _feedbackState.value = FeedbackState.Idle
    }
}
