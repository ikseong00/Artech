package org.ikseong.artech.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.ikseong.artech.analytics.AnalyticsEvents
import org.ikseong.artech.analytics.AnalyticsTracker
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.ui.component.ArticleCard
import org.ikseong.artech.ui.component.HomeSectionHeader
import org.ikseong.artech.ui.component.InterestTopicHubCard
import org.ikseong.artech.ui.component.RecommendedArticleCard
import org.ikseong.artech.ui.component.ScrollToTopFab
import org.ikseong.artech.util.PlatformBackHandler
import org.ikseong.artech.util.rememberExitAppAction
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (articleId: Long, link: String) -> Unit = { _, _ -> },
    onBlogClick: (String) -> Unit = {},
    onBlogListClick: () -> Unit = {},
    onLatestFeedClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    analyticsTracker: AnalyticsTracker = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val todayPicksListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val exitApp = rememberExitAppAction()
    val hasHomeContent = uiState.hasHomeContent()
    val homeListItemCount = uiState.homeListItemCount()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeUiEffect.ScrollToTop -> listState.scrollToItem(0)
                HomeUiEffect.ScrollRecommendedToStart -> todayPicksListState.scrollToItem(0)
            }
        }
    }

    LaunchedEffect(uiState.isLoading, hasHomeContent, homeListItemCount) {
        if (!uiState.isLoading && hasHomeContent && homeListItemCount > 0) {
            val (savedIndex, savedOffset) = viewModel.getSavedScrollPosition()
            if (savedIndex > 0) {
                listState.scrollToItem(
                    index = savedIndex.coerceAtMost(homeListItemCount - 1),
                    scrollOffset = savedOffset,
                )
                viewModel.clearScrollPosition()
            }
        }
    }

    PlatformBackHandler(enabled = listState.firstVisibleItemIndex > 0) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("스크롤 위치 저장") },
            text = { Text("다음에 들어올 때 현재 위치로 복원할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.saveScrollPosition(
                            listState.firstVisibleItemIndex,
                            listState.firstVisibleItemScrollOffset,
                        )
                        exitApp()
                    },
                ) {
                    Text("네")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        exitApp()
                    },
                ) {
                    Text("아니요")
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Artech",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            actions = {
                IconButton(onClick = onBlogListClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "블로그 목록",
                    )
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        when {
            uiState.isLoading && !hasHomeContent -> {
                LoadingState()
            }

            uiState.error != null && !hasHomeContent -> {
                ErrorState(
                    message = uiState.error ?: "오류가 발생했습니다",
                    onRetryClick = viewModel::loadHome,
                )
            }

            !hasHomeContent -> {
                EmptyState()
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (uiState.todayPicks.isNotEmpty()) {
                            item(key = "today_picks_header") {
                                HomeSectionHeader(
                                    title = "오늘의 추천",
                                    actionLabel = "새로고침",
                                    actionEnabled = !uiState.isRefreshingTodayPicks,
                                    actionLoading = uiState.isRefreshingTodayPicks,
                                    onActionClick = {
                                        analyticsTracker.logEvent(
                                            AnalyticsEvents.recommendationRefresh(source = "home"),
                                        )
                                        viewModel.refreshRecommendations()
                                    },
                                )
                            }

                            item(key = "today_picks_row") {
                                LazyRow(
                                    state = todayPicksListState,
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(
                                        items = uiState.todayPicks,
                                        key = { "today_${it.id}" },
                                    ) { article ->
                                        RecommendedArticleCard(
                                            article = article,
                                            onClick = {
                                                analyticsTracker.logArticleOpen(article, source = "home_today_picks")
                                                onArticleClick(article.id, article.link)
                                            },
                                            isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.interestTopics.isNotEmpty()) {
                            item(key = "interest_topics_header") {
                                HomeSectionHeader(title = "관심 주제로 보기")
                            }

                            item(key = "interest_topics_hub") {
                                InterestTopicHubCard(
                                    topics = uiState.interestTopics,
                                    unreadTotal = uiState.interestTopicUnreadTotal,
                                    onTopicClick = onTopicClick,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }

                        if (uiState.missedArticles.isNotEmpty()) {
                            item(key = "missed_articles_header") {
                                HomeSectionHeader(title = "놓친 글")
                            }

                            items(
                                items = uiState.missedArticles,
                                key = { "missed_${it.id}" },
                            ) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = {
                                        analyticsTracker.logArticleOpen(article, source = "home_missed")
                                        onArticleClick(article.id, article.link)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                                    onBlogClick = { blogSource ->
                                        analyticsTracker.logEvent(
                                            AnalyticsEvents.blogOpen(
                                                source = "home_missed",
                                                blogSource = blogSource,
                                            ),
                                        )
                                        onBlogClick(blogSource)
                                    },
                                )
                            }
                        }

                        if (uiState.latestPreview.isNotEmpty()) {
                            item(key = "latest_preview_header") {
                                HomeSectionHeader(
                                    title = "최신 글",
                                    actionLabel = "전체 보기",
                                    onActionClick = onLatestFeedClick,
                                )
                            }

                            items(
                                items = uiState.latestPreview,
                                key = { "latest_${it.id}" },
                            ) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = {
                                        analyticsTracker.logArticleOpen(article, source = "home_latest_preview")
                                        onArticleClick(article.id, article.link)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                                    onBlogClick = { blogSource ->
                                        analyticsTracker.logEvent(
                                            AnalyticsEvents.blogOpen(
                                                source = "home_latest_preview",
                                                blogSource = blogSource,
                                            ),
                                        )
                                        onBlogClick(blogSource)
                                    },
                                )
                            }
                        }
                    }

                    ScrollToTopFab(
                        visible = showScrollToTop,
                        onClick = {
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

private fun HomeUiState.hasHomeContent(): Boolean =
    todayPicks.isNotEmpty() ||
        interestTopics.isNotEmpty() ||
        missedArticles.isNotEmpty() ||
        latestPreview.isNotEmpty()

private fun HomeUiState.homeListItemCount(): Int =
    sectionItemCount(todayPicks.isNotEmpty(), 2) +
        sectionItemCount(interestTopics.isNotEmpty(), 2) +
        sectionItemCount(missedArticles.isNotEmpty(), 1 + missedArticles.size) +
        sectionItemCount(latestPreview.isNotEmpty(), 1 + latestPreview.size)

private fun sectionItemCount(hasSection: Boolean, count: Int): Int =
    if (hasSection) count else 0

private fun AnalyticsTracker.logArticleOpen(
    article: Article,
    source: String,
) {
    logEvent(
        AnalyticsEvents.articleOpen(
            articleId = article.id,
            source = source,
            category = article.category,
            blogSource = article.blogSource,
        ),
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetryClick) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("재시도")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "아티클이 없습니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
