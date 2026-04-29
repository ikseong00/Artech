package org.ikseong.artech.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.CategoryGroup
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.repository.ArticleRepository
import org.ikseong.artech.data.repository.FavoriteRepository
import org.ikseong.artech.data.repository.HistoryRepository
import org.ikseong.artech.data.repository.SettingsRepository
import org.ikseong.artech.data.repository.VisitSessionRepository
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
    private val visitSessionRepository: VisitSessionRepository,
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val interestProfileCalculator: HomeInterestProfileCalculator,
    private val feedComposer: HomeFeedComposer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(capacity = Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var loadJob: Job? = null
    private var hasRequestedInitialHome = false

    init {
        viewModelScope.launch {
            val lastVisit = visitSessionRepository.getSessionLastVisitTime()
            _uiState.update { it.copy(lastVisitTime = lastVisit) }
        }
        viewModelScope.launch {
            settingsRepository.interestCategories.collect { categories ->
                val previousCategories = _uiState.value.selectedInterestCategories
                _uiState.update { it.copy(selectedInterestCategories = categories) }
                if (!hasRequestedInitialHome || previousCategories != categories) {
                    hasRequestedInitialHome = true
                    loadHome(refreshingTodayPicks = false)
                }
            }
        }
    }

    fun loadHome() {
        loadHome(refreshingTodayPicks = false)
    }

    fun refreshRecommendations() {
        if (_uiState.value.isRefreshingTodayPicks) return
        loadHome(refreshingTodayPicks = true)
    }

    fun toggleInterestCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.toggleInterestCategory(category)
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        viewModelScope.launch {
            settingsRepository.saveScrollPosition(index, offset)
        }
    }

    fun clearScrollPosition() {
        viewModelScope.launch {
            settingsRepository.clearScrollPosition()
        }
    }

    suspend fun getSavedScrollPosition(): Pair<Int, Int> =
        settingsRepository.getScrollPosition()

    private fun loadHome(refreshingTodayPicks: Boolean) {
        loadJob?.cancel()
        _uiState.update {
            if (refreshingTodayPicks) {
                it.copy(isRefreshingTodayPicks = true, error = null)
            } else {
                it.copy(isLoading = true, isRefreshingTodayPicks = false, error = null)
            }
        }

        loadJob = viewModelScope.launch {
            try {
                val homeLoadResult = loadHomeSections()
                val sections = homeLoadResult.sections
                settingsRepository.addRecommendedArticleIdsSeenToday(
                    sections.todayPicks.map { it.id } +
                        sections.interestCategoryRecommendations.flatMap { recommendation ->
                            recommendation.articles.map { it.id }
                        },
                )
                _uiState.update {
                    it.copy(
                        randomBannerArticle = sections.randomBannerArticle,
                        todayPicks = sections.todayPicks,
                        interestCategoryRecommendations = sections.interestCategoryRecommendations,
                        interestTopics = sections.interestTopics,
                        interestTopicUnreadTotal = sections.interestTopicUnreadTotal,
                        availableCategories = homeLoadResult.availableCategories,
                        selectedInterestCategories = homeLoadResult.selectedInterestCategories,
                        missedArticles = sections.missedArticles,
                        latestPreview = sections.latestPreview,
                        isLoading = false,
                        isRefreshingTodayPicks = false,
                        error = null,
                    )
                }
                if (refreshingTodayPicks) {
                    _uiEffect.trySend(HomeUiEffect.ScrollRecommendedToStart)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshingTodayPicks = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    private suspend fun loadHomeSections(): HomeLoadResult {
        val now = currentInstant()
        val seenTodayPickIds = settingsRepository.getRecommendedArticleIdsSeenToday()
        val selectedInterestCategories = settingsRepository.interestCategories.first()
        val candidates = articleRepository.getHomeFeedCandidates()
        val availableCategories = runCatching {
            articleRepository.getCategories().sorted()
        }.getOrElse {
            CategoryGroup.mergeCategories(candidates.mapNotNull { article -> article.category }).sorted()
        }
        val readHistory = historyRepository.getAllWithReadAt().first()
        val favorites = favoriteRepository.getAllWithSavedAt().first()
        val profile = interestProfileCalculator.calculate(
            readHistory = readHistory,
            favorites = favorites,
            now = now,
        )
        val sections = composeHomeSections(
            feedComposer = feedComposer,
            candidates = candidates,
            readArticleIds = readHistory.map { it.article.id }.toSet(),
            profile = profile,
            selectedInterestCategories = selectedInterestCategories,
            seenTodayPickIds = seenTodayPickIds,
            now = now,
        )
        return HomeLoadResult(
            sections = sections,
            availableCategories = availableCategories,
            selectedInterestCategories = selectedInterestCategories,
        )
    }

    private fun currentInstant(): Instant =
        Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())

}

private data class HomeLoadResult(
    val sections: HomeFeedSections,
    val availableCategories: List<String>,
    val selectedInterestCategories: List<String>,
)

internal fun composeHomeSections(
    feedComposer: HomeFeedComposer,
    candidates: List<Article>,
    readArticleIds: Set<Long>,
    profile: HomeInterestProfile,
    selectedInterestCategories: List<String> = emptyList(),
    seenTodayPickIds: Set<Long>,
    now: Instant,
): HomeFeedSections {
    val baseSections = feedComposer.compose(
        candidates = candidates,
        readArticleIds = readArticleIds,
        profile = profile,
        selectedInterestCategories = selectedInterestCategories,
        now = now,
    )
    if (seenTodayPickIds.isEmpty()) return baseSections

    val nextTodayPicks = feedComposer.compose(
        candidates = candidates.filter { it.id !in seenTodayPickIds },
        readArticleIds = readArticleIds,
        profile = profile,
        selectedInterestCategories = selectedInterestCategories,
        now = now,
    ).todayPicks

    return if (nextTodayPicks.isEmpty()) {
        baseSections
    } else {
        baseSections.copy(todayPicks = nextTodayPicks)
    }
}
