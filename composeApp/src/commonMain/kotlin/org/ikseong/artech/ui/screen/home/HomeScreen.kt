package org.ikseong.artech.ui.screen.home

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.ikseong.artech.ui.component.ArticleCard
import org.ikseong.artech.ui.component.CategoryFilterRow
import org.ikseong.artech.ui.component.RecommendedArticleCard
import org.ikseong.artech.ui.component.ScrollToTopFab
import org.ikseong.artech.util.PlatformBackHandler
import org.ikseong.artech.util.rememberExitAppAction
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (articleId: Long, link: String) -> Unit = { _, _ -> },
    onBlogClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val exitApp = rememberExitAppAction()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    var showExitDialog by remember { mutableStateOf(false) }

    // 스크롤 방향 감지 → 필터 숨김/표시
    var isFilterVisible by remember { mutableStateOf(true) }
    var accumulatedDelta by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    var filterHeightDp by remember { mutableStateOf(0.dp) }
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentOffset) ->
            val delta = (currentIndex - previousIndex) * 500 + (currentOffset - previousOffset)
            accumulatedDelta = (accumulatedDelta + delta).coerceIn(-500, 500)

            if (accumulatedDelta > 200) {
                isFilterVisible = false
            } else if (accumulatedDelta < -200 || (currentIndex == 0 && currentOffset == 0)) {
                isFilterVisible = true
            }

            previousIndex = currentIndex
            previousOffset = currentOffset
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeUiEffect.ScrollToTop -> listState.scrollToItem(0)
            }
        }
    }

    // 앱 시작 시 저장된 스크롤 위치 자동 복원
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.articles.isNotEmpty()) {
            val (savedIndex, savedOffset) = viewModel.getSavedScrollPosition()
            if (savedIndex > 0) {
                listState.scrollToItem(savedIndex, savedOffset)
                viewModel.clearScrollPosition()
            }
        }
    }

    // 홈에서 뒤로가기 → 스크롤 위치 저장 여부 확인
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
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        when {
            uiState.isLoading && uiState.displayArticles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.displayArticles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "오류가 발생했습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::loadArticles) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Text("재시도")
                        }
                    }
                }
            }

            uiState.displayArticles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.showUnreadOnly && uiState.articles.isNotEmpty()) {
                            "모든 아티클을 읽었습니다"
                        } else {
                            "아티클이 없습니다"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    val topPadding = if (isFilterVisible) filterHeightDp else 0.dp
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = topPadding, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (uiState.recommendedArticles.isNotEmpty() && uiState.selectedCategory == null) {
                            item(key = "recommended_header") {
                                Text(
                                    text = "오늘의 추천",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                )
                            }

                            item(key = "recommended_row") {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(
                                        items = uiState.recommendedArticles,
                                        key = { "rec_${it.id}" },
                                    ) { article ->
                                        RecommendedArticleCard(
                                            article = article,
                                            onClick = { onArticleClick(article.id, article.link) },
                                        )
                                    }
                                }
                            }

                            item(key = "latest_header") {
                                Text(
                                    text = "최신 아티클",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                                )
                            }
                        }

                        items(
                            items = uiState.displayArticles,
                            key = { it.id },
                        ) { article ->
                            ArticleCard(
                                article = article,
                                onClick = { onArticleClick(article.id, article.link) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                                isNew = uiState.lastVisitTime?.let { article.displayDate > it } ?: false,
                                onBlogClick = onBlogClick,
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    // 필터를 LazyColumn 위에 오버레이
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isFilterVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                        modifier = Modifier.align(Alignment.TopStart),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .onGloballyPositioned { coordinates ->
                                    filterHeightDp = with(density) {
                                        coordinates.size.height.toDp()
                                    }
                                },
                        ) {
                            CategoryFilterRow(
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = viewModel::selectCategory,
                                categories = uiState.categories,
                            )

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FilterChip(
                                    selected = uiState.showUnreadOnly,
                                    onClick = viewModel::toggleUnreadFilter,
                                    label = { Text("안 본 글만", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (uiState.showUnreadOnly) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
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
