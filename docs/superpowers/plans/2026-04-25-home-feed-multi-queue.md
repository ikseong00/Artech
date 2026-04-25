# Multi-Queue Home Feed Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current scroll-heavy home feed with a multi-queue home screen backed by local-interest personalization, with a clear `관심 주제로 보기` hub between recommendations and the feed list.

**Architecture:** Keep `ArticleRepository` as the raw article source, add a `HomeInterestProfileCalculator` plus `HomeFeedComposer` for home-specific ranking, and split the current home list flow into a new `LatestFeedScreen` route. The new `HomeViewModel` becomes an orchestrator for section state instead of owning recommendation logic directly. The middle home section is rendered as one topic hub card with topic chips, not a row of article-like topic cards.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin, Navigation Compose, Room KMP, DataStore, kotlin.test

---

## File Map

### Create

- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/model/SavedFavoriteArticle.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfile.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculator.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedSections.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposer.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiState.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/HomeSectionHeader.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/InterestTopicHubCard.kt`
- `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculatorTest.kt`
- `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt`
- `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiStateTest.kt`

### Modify

- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/local/entity/EntityMapper.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/FavoriteRepository.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/ArticleRepository.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/SettingsRepository.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/Route.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeUiState.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeScreen.kt`

### Keep As-Is But Reuse

- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/ArticleCard.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/CategoryFilterRow.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/ScrollToTopFab.kt`
- `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/HistoryRepository.kt`

## Task 1: Add Interest Profile Inputs and Calculator

**Files:**
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/model/SavedFavoriteArticle.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfile.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculator.kt`
- Create: `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculatorTest.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/local/entity/EntityMapper.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/FavoriteRepository.kt`

- [ ] **Step 1: Write the failing calculator test**

```kotlin
package org.ikseong.artech.ui.screen.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.HistoryArticle
import org.ikseong.artech.data.model.SavedFavoriteArticle

class HomeInterestProfileCalculatorTest {

    private val now = Instant.parse("2026-04-25T12:00:00Z")
    private val calculator = HomeInterestProfileCalculator()

    @Test
    fun `favorite signal outweighs read signal while preserving recency`() {
        val reads = listOf(
            HistoryArticle(
                article = article(id = 1, category = "Android", blog = "Kakao"),
                readAt = Instant.parse("2026-04-24T12:00:00Z"),
            ),
            HistoryArticle(
                article = article(id = 2, category = "Server", blog = "Toss"),
                readAt = Instant.parse("2026-04-10T12:00:00Z"),
            ),
        )
        val favorites = listOf(
            SavedFavoriteArticle(
                article = article(id = 3, category = "AI", blog = "OpenAI"),
                savedAt = Instant.parse("2026-04-23T12:00:00Z"),
            ),
        )

        val profile = calculator.calculate(
            readHistory = reads,
            favorites = favorites,
            now = now,
        )

        assertEquals("AI", profile.topCategories.first())
        assertTrue(profile.categoryScores.getValue("AI") > profile.categoryScores.getValue("Android"))
        assertTrue(profile.blogScores.getValue("OpenAI") > profile.blogScores.getValue("Kakao"))
    }

    private fun article(id: Long, category: String, blog: String) = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = category,
        blogSource = blog,
        publishedAt = Instant.parse("2026-04-25T00:00:00Z"),
        createdAt = Instant.parse("2026-04-25T00:00:00Z"),
    )
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: FAIL with unresolved references for `SavedFavoriteArticle`, `HomeInterestProfile`, or `HomeInterestProfileCalculator`.

- [ ] **Step 3: Add favorite timestamp model, repository accessor, and calculator**

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/data/model/SavedFavoriteArticle.kt
package org.ikseong.artech.data.model

import kotlinx.datetime.Instant

data class SavedFavoriteArticle(
    val article: Article,
    val savedAt: Instant,
)
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfile.kt
package org.ikseong.artech.ui.screen.home

data class HomeInterestProfile(
    val categoryScores: Map<String, Double> = emptyMap(),
    val blogScores: Map<String, Double> = emptyMap(),
) {
    val topCategories: List<String>
        get() = categoryScores.entries
            .sortedByDescending { it.value }
            .map { it.key }

    fun scoreForCategory(category: String?): Double =
        category?.let { categoryScores[it] } ?: 0.0

    fun scoreForBlog(blogSource: String): Double =
        blogScores[blogSource] ?: 0.0
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculator.kt
package org.ikseong.artech.ui.screen.home

import kotlin.math.max
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.daysUntil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ikseong.artech.data.model.HistoryArticle
import org.ikseong.artech.data.model.SavedFavoriteArticle

class HomeInterestProfileCalculator {

    fun calculate(
        readHistory: List<HistoryArticle>,
        favorites: List<SavedFavoriteArticle>,
        now: Instant = Clock.System.now(),
    ): HomeInterestProfile {
        val categoryScores = linkedMapOf<String, Double>()
        val blogScores = linkedMapOf<String, Double>()

        readHistory.forEach { item ->
            val weight = 1.0 * recencyMultiplier(item.readAt, now)
            item.article.category?.let { categoryScores[it] = categoryScores.getOrDefault(it, 0.0) + weight }
            blogScores[item.article.blogSource] = blogScores.getOrDefault(item.article.blogSource, 0.0) + weight
        }

        favorites.forEach { item ->
            val weight = 3.0 * recencyMultiplier(item.savedAt, now)
            item.article.category?.let { categoryScores[it] = categoryScores.getOrDefault(it, 0.0) + weight }
            blogScores[item.article.blogSource] = blogScores.getOrDefault(item.article.blogSource, 0.0) + weight
        }

        return HomeInterestProfile(
            categoryScores = categoryScores,
            blogScores = blogScores,
        )
    }

    private fun recencyMultiplier(eventAt: Instant, now: Instant): Double {
        val days = max(
            0,
            eventAt.toLocalDateTime(TimeZone.UTC).date.daysUntil(now.toLocalDateTime(TimeZone.UTC).date),
        )
        return when {
            days <= 2 -> 1.0
            days <= 7 -> 0.75
            days <= 14 -> 0.5
            else -> 0.25
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/data/local/entity/EntityMapper.kt
fun FavoriteEntity.toSavedFavoriteArticle(): SavedFavoriteArticle = SavedFavoriteArticle(
    article = toArticle(),
    savedAt = Instant.fromEpochMilliseconds(savedAt),
)
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/FavoriteRepository.kt
fun getAllWithSavedAt(): Flow<List<SavedFavoriteArticle>> =
    favoriteDao.getAll().map { entities -> entities.map { it.toSavedFavoriteArticle() } }
```

- [ ] **Step 4: Run the calculator test to verify it passes**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: PASS for `HomeInterestProfileCalculatorTest` and `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/model/SavedFavoriteArticle.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/local/entity/EntityMapper.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/FavoriteRepository.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfile.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculator.kt \
  composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeInterestProfileCalculatorTest.kt
git commit -m "feat: add home interest profile calculator"
```

## Task 2: Add Home Feed Composer and Candidate Query

**Files:**
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedSections.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposer.kt`
- Create: `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/ArticleRepository.kt`

- [ ] **Step 1: Write the failing composer test**

```kotlin
package org.ikseong.artech.ui.screen.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

class HomeFeedComposerTest {

    private val now = Instant.parse("2026-04-25T12:00:00Z")
    private val composer = HomeFeedComposer()

    @Test
    fun `compose creates diversified today picks and missed articles`() {
        val profile = HomeInterestProfile(
            categoryScores = mapOf("Android" to 5.0, "AI" to 4.0, "Server" to 2.0),
            blogScores = mapOf("Kakao" to 3.0, "OpenAI" to 3.5),
        )
        val candidates = listOf(
            article(1, "Android", "Kakao", "2026-04-25T08:00:00Z"),
            article(2, "Android", "Kakao", "2026-04-25T07:00:00Z"),
            article(3, "AI", "OpenAI", "2026-04-24T20:00:00Z"),
            article(4, "Server", "Toss", "2026-04-18T20:00:00Z"),
            article(5, "AI", "Anthropic", "2026-04-22T20:00:00Z"),
        )

        val result = composer.compose(
            candidates = candidates,
            readArticleIds = setOf(2L),
            profile = profile,
            now = now,
        )

        assertEquals(listOf(1L, 3L, 4L, 5L), result.todayPicks.map { it.id })
        assertTrue(result.missedArticles.none { missed -> missed.id in result.todayPicks.map { it.id } })
        assertEquals("Android", result.interestTopics.first().category)
        assertEquals(4, result.interestTopicUnreadTotal)
    }

    private fun article(id: Long, category: String, blog: String, publishedAt: String) = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = category,
        blogSource = blog,
        publishedAt = Instant.parse(publishedAt),
        createdAt = Instant.parse(publishedAt),
    )
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: FAIL because `HomeFeedComposer` and `HomeFeedSections` do not exist.

- [ ] **Step 3: Implement home feed section models, composer, and candidate query**

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedSections.kt
package org.ikseong.artech.ui.screen.home

import org.ikseong.artech.data.model.Article

data class InterestTopicShortcut(
    val category: String,
    val unreadCount: Int,
)

data class HomeFeedSections(
    val todayPicks: List<Article> = emptyList(),
    val interestTopics: List<InterestTopicShortcut> = emptyList(),
    val interestTopicUnreadTotal: Int = 0,
    val missedArticles: List<Article> = emptyList(),
    val latestPreview: List<Article> = emptyList(),
)
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposer.kt
package org.ikseong.artech.ui.screen.home

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.daysUntil
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ikseong.artech.data.model.Article

class HomeFeedComposer {

    fun compose(
        candidates: List<Article>,
        readArticleIds: Set<Long>,
        profile: HomeInterestProfile,
        now: Instant = Clock.System.now(),
    ): HomeFeedSections {
        val unreadCandidates = candidates.filterNot { it.id in readArticleIds }
        val ranked = unreadCandidates
            .sortedByDescending { score(it, profile, now) }

        val todayPicks = mutableListOf<Article>()
        val selectedCategories = mutableSetOf<String>()

        ranked.forEach { article ->
            if (todayPicks.size >= 5) return@forEach
            val category = article.category.orEmpty()
            if (category.isNotEmpty() && category in selectedCategories) return@forEach
            todayPicks += article
            if (category.isNotEmpty()) selectedCategories += category
        }

        ranked.forEach { article ->
            if (todayPicks.size >= 5) return@forEach
            if (todayPicks.any { it.id == article.id }) return@forEach
            todayPicks += article
        }
        val topicCategories = (profile.topCategories + unreadCandidates.mapNotNull { it.category })
            .distinct()
        val interestTopics = topicCategories
            .map { category ->
                InterestTopicShortcut(
                    category = category,
                    unreadCount = unreadCandidates.count { it.category == category },
                )
            }
            .filter { it.unreadCount > 0 }
            .take(5)
        val interestTopicUnreadTotal = interestTopics.sumOf { it.unreadCount }
        val missedArticles = ranked
            .filter { article -> article.id !in todayPicks.map { it.id } }
            .filter { article -> ageInDays(article, now) in 2..7 }
            .take(3)
        val latestPreview = candidates.take(4)

        return HomeFeedSections(
            todayPicks = todayPicks,
            interestTopics = interestTopics,
            interestTopicUnreadTotal = interestTopicUnreadTotal,
            missedArticles = missedArticles,
            latestPreview = latestPreview,
        )
    }

    private fun score(article: Article, profile: HomeInterestProfile, now: Instant): Double {
        val categoryScore = profile.scoreForCategory(article.category)
        val blogScore = profile.scoreForBlog(article.blogSource)
        val freshnessScore = when (ageInDays(article, now)) {
            in 0..1 -> 3.0
            in 2..3 -> 2.0
            in 4..7 -> 1.0
            else -> 0.25
        }
        return categoryScore + blogScore + freshnessScore
    }

    private fun ageInDays(article: Article, now: Instant): Int =
        article.displayDate.toLocalDateTime(TimeZone.UTC).date.daysUntil(now.toLocalDateTime(TimeZone.UTC).date)
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/ArticleRepository.kt
suspend fun getHomeFeedCandidates(limit: Int = HOME_FEED_CANDIDATE_SIZE): List<Article> {
    return client.from(TABLE_NAME)
        .select {
            filter { neq("primary_category", EXCLUDED_CATEGORY) }
            order("published_at", Order.DESCENDING)
            range(0, (limit - 1).toLong())
        }
        .decodeList<ArticleDto>()
        .map { it.toArticle() }
}

companion object {
    const val DEFAULT_PAGE_SIZE = 20
    const val HOME_FEED_CANDIDATE_SIZE = 100
}
```

- [ ] **Step 4: Run the composer test to verify it passes**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: PASS for both `HomeInterestProfileCalculatorTest` and `HomeFeedComposerTest`.

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/ArticleRepository.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedSections.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposer.kt \
  composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt
git commit -m "feat: compose multi-queue home feed sections"
```

## Task 3: Extract the Current List Feed into LatestFeedScreen

**Files:**
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiState.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt`
- Create: `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiStateTest.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/Route.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt`

- [ ] **Step 1: Write the failing state regression test for unread filtering**

```kotlin
package org.ikseong.artech.ui.screen.latest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

class LatestFeedUiStateTest {

    @Test
    fun `display articles hides read ids only when unread filter is enabled`() {
        val first = article(1)
        val second = article(2)

        val unreadOnly = LatestFeedUiState(
            articles = listOf(first, second),
            readArticleIds = setOf(2L),
            showUnreadOnly = true,
        )
        val allArticles = unreadOnly.copy(showUnreadOnly = false)

        assertEquals(listOf(first), unreadOnly.displayArticles)
        assertEquals(listOf(first, second), allArticles.displayArticles)
    }

    private fun article(id: Long) = Article(
        id = id,
        title = "Article $id",
        link = "https://example.com/$id",
        summary = null,
        category = "Android",
        blogSource = "Kakao",
        publishedAt = Instant.parse("2026-04-25T00:00:00Z"),
        createdAt = Instant.parse("2026-04-25T00:00:00Z"),
    )
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: FAIL because `LatestFeedUiState` does not exist.

- [ ] **Step 3: Create the latest-feed route, state, viewmodel, and screen by copying the current list behavior out of Home**

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/Route.kt
@Serializable
data class LatestFeed(val initialCategory: String? = null) : Route
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiState.kt
package org.ikseong.artech.ui.screen.latest

import kotlinx.datetime.Instant
import org.ikseong.artech.data.model.Article

data class LatestFeedUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val hasMorePages: Boolean = true,
    val lastVisitTime: Instant? = null,
    val showUnreadOnly: Boolean = false,
    val readArticleIds: Set<Long> = emptySet(),
) {
    val displayArticles: List<Article>
        get() = if (showUnreadOnly) {
            articles.filterNot { it.id in readArticleIds }
        } else {
            articles
        }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt
class LatestFeedViewModel(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Route.LatestFeed>()
    private val _uiState = MutableStateFlow(LatestFeedUiState())
    val uiState: StateFlow<LatestFeedUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val lastVisit = settingsRepository.lastVisitTime.first()
            _uiState.update {
                it.copy(
                    selectedCategory = route.initialCategory,
                    lastVisitTime = lastVisit,
                )
            }
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

    fun loadArticles() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.copy(isLoading = true, error = null, hasMorePages = true) }

        loadJob = viewModelScope.launch {
            runCatching {
                articleRepository.getArticles(
                    category = _uiState.value.selectedCategory,
                    offset = 0,
                )
            }.onSuccess { articles ->
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMorePages) return

        currentPage += 1
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            runCatching {
                articleRepository.getArticles(
                    category = _uiState.value.selectedCategory,
                    offset = currentPage * ArticleRepository.DEFAULT_PAGE_SIZE,
                )
            }.onSuccess { articles ->
                _uiState.update {
                    it.copy(
                        articles = (it.articles + articles).distinctBy { article -> article.id },
                        isLoadingMore = false,
                        hasMorePages = articles.size >= ArticleRepository.DEFAULT_PAGE_SIZE,
                    )
                }
            }.onFailure { error ->
                currentPage -= 1
                _uiState.update { it.copy(isLoadingMore = false, error = error.message) }
            }
        }
    }

    fun selectCategory(category: String?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadArticles()
    }

    fun toggleUnreadFilter() { _uiState.update { it.copy(showUnreadOnly = !it.showUnreadOnly) } }

    private suspend fun loadCategories() {
        _uiState.update { it.copy(categories = articleRepository.getCategories()) }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt
@Composable
fun LatestFeedScreen(
    onArticleClick: (Long, String) -> Unit,
    onBlogClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LatestFeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("최신 글") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            },
        )

        CategoryFilterRow(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = viewModel::selectCategory,
            categories = uiState.categories,
        )

        LazyColumn(state = listState) {
            items(uiState.displayArticles, key = { it.id }) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article.id, article.link) },
                    onBlogClick = onBlogClick,
                    isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                )
            }
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt
viewModelOf(::LatestFeedViewModel)
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt
composable<Route.LatestFeed> {
    LatestFeedScreen(
        onArticleClick = { articleId, link ->
            navController.navigate(Route.Detail(articleId = articleId, link = link))
        },
        onBlogClick = { blogSource ->
            navController.navigate(Route.Blog(blogSource = blogSource))
        },
        onBack = { navController.popBackStack() },
    )
}
```

- [ ] **Step 4: Run state tests and Android compilation**

Run: `./gradlew :composeApp:iosSimulatorArm64Test :composeApp:compileDebugKotlinAndroid --rerun-tasks`

Expected: PASS for `LatestFeedUiStateTest` and successful Android compilation with the new route wired.

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiState.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/Route.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt \
  composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedUiStateTest.kt
git commit -m "feat: move list browsing to latest feed screen"
```

## Task 4: Rewrite Home State Around Multi-Queue Sections

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeUiState.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/SettingsRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt`
- Modify: `composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt`

- [ ] **Step 1: Write the failing home-state regression test for cold start fallback**

```kotlin
// Add this test method to composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt
    @Test
    fun `cold start still produces a today picks section`() {
        val candidates = listOf(
            article(1, "Android", "Kakao", "2026-04-25T08:00:00Z"),
            article(2, "AI", "OpenAI", "2026-04-25T07:00:00Z"),
            article(3, "Server", "Toss", "2026-04-24T20:00:00Z"),
        )

        val result = composer.compose(
            candidates = candidates,
            readArticleIds = emptySet(),
            profile = HomeInterestProfile(),
            now = Instant.parse("2026-04-25T12:00:00Z"),
        )

        assertTrue(result.todayPicks.isNotEmpty())
        assertEquals(listOf("Android", "AI", "Server"), result.interestTopics.map { it.category })
        assertEquals(3, result.interestTopicUnreadTotal)
    }
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :composeApp:iosSimulatorArm64Test --rerun-tasks`

Expected: FAIL because `compose()` does not yet guarantee non-empty `todayPicks` and default topic chips when `HomeInterestProfile()` is empty.

- [ ] **Step 3: Rewrite HomeUiState and HomeViewModel to load sections instead of a long list**

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeUiState.kt
data class HomeUiState(
    val todayPicks: List<Article> = emptyList(),
    val interestTopics: List<InterestTopicShortcut> = emptyList(),
    val interestTopicUnreadTotal: Int = 0,
    val missedArticles: List<Article> = emptyList(),
    val latestPreview: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshingTodayPicks: Boolean = false,
    val error: String? = null,
    val lastVisitTime: Instant? = null,
)
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt
class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val interestProfileCalculator: HomeInterestProfileCalculator,
    private val homeFeedComposer: HomeFeedComposer,
) : ViewModel() {

    fun loadHome() {
        viewModelScope.launch {
            val candidates = articleRepository.getHomeFeedCandidates()
            val history = historyRepository.getAllWithReadAt().first()
            val favorites = favoriteRepository.getAllWithSavedAt().first()
            val profile = interestProfileCalculator.calculate(
                readHistory = history,
                favorites = favorites,
            )
            val sections = homeFeedComposer.compose(
                candidates = candidates,
                readArticleIds = history.map { it.article.id }.toSet(),
                profile = profile,
            )
            _uiState.update {
                it.copy(
                    todayPicks = sections.todayPicks,
                    interestTopics = sections.interestTopics,
                    interestTopicUnreadTotal = sections.interestTopicUnreadTotal,
                    missedArticles = sections.missedArticles,
                    latestPreview = sections.latestPreview,
                    isLoading = false,
                )
            }
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/SettingsRepository.kt
companion object {
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val LAST_VISIT_TIME_KEY = longPreferencesKey("last_visit_time")
    private val SCROLL_POSITION_KEY = intPreferencesKey("scroll_position")
    private val SCROLL_OFFSET_KEY = intPreferencesKey("scroll_offset")
    private val SKIPPED_OPTIONAL_VERSION_KEY = stringPreferencesKey("skipped_optional_version")
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt
single { HomeInterestProfileCalculator() }
single { HomeFeedComposer() }
```

- [ ] **Step 4: Run tests and compile after the home-state rewrite**

Run: `./gradlew :composeApp:iosSimulatorArm64Test :composeApp:compileDebugKotlinAndroid --rerun-tasks`

Expected: PASS for profile/composer tests and successful Android compilation with the new DI graph.

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/SettingsRepository.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/di/AppModule.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeUiState.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt \
  composeApp/src/commonTest/kotlin/org/ikseong/artech/ui/screen/home/HomeFeedComposerTest.kt
git commit -m "feat: load home as multi-queue sections"
```

## Task 5: Render the Multi-Queue Home UI and Hook Navigation

**Files:**
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/HomeSectionHeader.kt`
- Create: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/InterestTopicHubCard.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt`

- [ ] **Step 1: Write the failing smoke checklist for the new home layout**

```text
Manual smoke expectations before coding:
1. Home no longer shows the old category filter row at the top.
2. Home shows sections in this order: today picks, interest topic hub, missed articles, latest preview.
3. "Latest" CTA opens Route.LatestFeed.
4. Topic chip tap opens Route.LatestFeed with that initial category.
5. Article tap still opens DetailScreen.
```

- [ ] **Step 2: Run Android compilation to capture the current failing baseline**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid --rerun-tasks`

Expected: Either compile passes with the old screen or fails once the new `HomeUiState` no longer matches `HomeScreen`.

- [ ] **Step 3: Replace the old HomeScreen list UI with section blocks and wire "more latest" navigation**

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/HomeSectionHeader.kt
@Composable
fun HomeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) { Text(actionLabel) }
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/InterestTopicHubCard.kt
@Composable
fun InterestTopicHubCard(
    topics: List<InterestTopicShortcut>,
    unreadTotal: Int,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "추천이 애매하면 주제로 좁혀보세요",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "읽기 이력 기준으로 안 본 글이 많은 관심 주제입니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${unreadTotal}개",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                topics.forEach { topic ->
                    Text(
                        text = "${CategoryGroup.toDisplayName(topic.category)} ${topic.unreadCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onClick(topic.category) }
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeScreen.kt
@Composable
fun HomeScreen(
    onArticleClick: (Long, String) -> Unit = { _, _ -> },
    onBlogClick: (String) -> Unit = {},
    onBlogListClick: () -> Unit = {},
    onLatestFeedClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item("topbar") {
            TopAppBar(
                title = { Text("Artech") },
                actions = {
                    IconButton(onClick = onBlogListClick) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "블로그 목록")
                    }
                },
            )
        }
        item("today_picks") {
            Column {
                HomeSectionHeader(title = "오늘의 추천")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.todayPicks, key = { it.id }) { article ->
                        RecommendedArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.id, article.link) },
                            isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                        )
                    }
                }
            }
        }
        item("topics") {
            HomeSectionHeader(title = "관심 주제로 보기")
            InterestTopicHubCard(
                topics = uiState.interestTopics,
                unreadTotal = uiState.interestTopicUnreadTotal,
                onClick = onTopicClick,
            )
        }
        item("missed") {
            Column {
                HomeSectionHeader(title = "놓치기 쉬운 글")
                uiState.missedArticles.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id, article.link) },
                        onBlogClick = onBlogClick,
                    )
                }
            }
        }
        item("latest_preview") {
            Column {
                HomeSectionHeader(
                    title = "최신 글",
                    actionLabel = "전체 보기",
                    onActionClick = onLatestFeedClick,
                )
                uiState.latestPreview.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id, article.link) },
                        onBlogClick = onBlogClick,
                    )
                }
            }
        }
    }
}
```

```kotlin
// composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt
HomeScreen(
    onArticleClick = { articleId, link ->
        navController.navigate(Route.Detail(articleId = articleId, link = link))
    },
    onBlogClick = { blogSource ->
        navController.navigate(Route.Blog(blogSource = blogSource))
    },
    onBlogListClick = {
        navController.navigate(Route.BlogList)
    },
    onLatestFeedClick = {
        navController.navigate(Route.LatestFeed())
    },
    onTopicClick = { category ->
        navController.navigate(Route.LatestFeed(initialCategory = category))
    },
)
```

- [ ] **Step 4: Run Android compilation and manual smoke test**

Run: `./gradlew :composeApp:compileDebugKotlinAndroid --rerun-tasks`

Expected: `BUILD SUCCESSFUL`.

Manual smoke:

1. Launch the app.
2. Confirm Home shows section blocks instead of the old filter-first list.
3. Confirm `관심 주제로 보기` renders as one hub card with topic chips and unread counts.
4. Tap one article in `오늘의 추천`; verify `DetailScreen` opens.
5. Tap one topic chip; verify `LatestFeedScreen` opens with that category selected.
6. Tap `전체 보기` in the latest section; verify `LatestFeedScreen` opens.

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/HomeSectionHeader.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/component/InterestTopicHubCard.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeScreen.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/navigation/AppNavigation.kt
git commit -m "feat: render multi-queue home screen"
```

## Task 6: Final Verification and Cleanup

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt`
- Modify: `docs/superpowers/specs/2026-04-25-home-feed-multi-queue-design.md` (only if implementation diverged and needs a note)

- [ ] **Step 1: Write the final verification checklist**

```text
Verification checklist:
1. Cold start shows non-empty today picks and a non-empty `관심 주제로 보기` hub.
2. Home hides old recommendation refresh quota UI.
3. LatestFeedScreen owns category filter + unread filter + pagination.
4. Returning from detail keeps both Home and LatestFeed scroll position behavior intact.
5. No compile errors from removed SettingsRepository recommendation APIs.
```

- [ ] **Step 2: Run full automated verification**

Run: `./gradlew :composeApp:allTests :composeApp:compileDebugKotlinAndroid --rerun-tasks`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Do manual regression verification**

```text
Manual regression path:
1. Open Home and verify all four sections render.
2. Open an article from Home, go back, verify Home position is preserved.
3. Open LatestFeedScreen from Home, toggle category and unread filters, scroll, open detail, go back.
4. Verify LatestFeedScreen still restores its list state and pagination works.
5. Open Favorites and History to confirm unchanged navigation paths still work.
```

- [ ] **Step 4: Remove dead code and align naming**

```kotlin
// Remove these declarations entirely:
// sealed interface HomeUiEffect { data object ScrollToTop : HomeUiEffect; data object ScrollRecommendedToStart : HomeUiEffect }
// val recommendedArticles: List<Article> = emptyList()
// val recommendRefreshRemaining: Int = SettingsRepository.MAX_RECOMMEND_REFRESHES
// fun refreshRecommendations()
// private val RECOMMEND_REFRESH_DATE_KEY = stringPreferencesKey("recommend_refresh_date")
// private val RECOMMEND_REFRESH_COUNT_KEY = intPreferencesKey("recommend_refresh_count")
// private val RECOMMEND_SEEN_ARTICLE_IDS_DATE_KEY = stringPreferencesKey("recommend_seen_article_ids_date")
// private val RECOMMEND_SEEN_ARTICLE_IDS_KEY = stringSetPreferencesKey("recommend_seen_article_ids")
```

- [ ] **Step 5: Commit**

```bash
git add \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/data/repository/SettingsRepository.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeUiState.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/home/HomeViewModel.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedViewModel.kt \
  composeApp/src/commonMain/kotlin/org/ikseong/artech/ui/screen/latest/LatestFeedScreen.kt
git commit -m "chore: finalize multi-queue home feed rollout"
```

## Spec Coverage Check

- `오늘의 추천`, `관심 주제로 보기`, `놓치기 쉬운 글`, `최신 글` 섹션: Task 2, Task 4, Task 5
- 로컬 관심사 계산(읽기이력 + 즐겨찾기 가중치): Task 1
- 넓은 홈 후보 기사 풀: Task 2
- 최신 피드 분리 (`LatestFeedScreen`): Task 3, Task 5
- 홈 상단 카테고리 필터 제거 및 최신 화면 이동: Task 3, Task 5
- cold start / empty state / partial failure handling: Task 4, Task 6
- 스크롤 복귀 및 회귀 검증: Task 3, Task 6

## Self-Review Notes

- No placeholder implementation text remains.
- All file paths are concrete and match the current repository layout.
- UI-heavy tasks use compile + manual smoke checks because the repo does not currently have Compose UI test infrastructure.
