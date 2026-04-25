package org.ikseong.artech.ui.screen.latest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.ikseong.artech.ui.component.ArticleCard
import org.ikseong.artech.ui.component.CategoryFilterRow
import org.ikseong.artech.ui.component.ScrollToTopFab
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestFeedScreen(
    onArticleClick: (articleId: Long, link: String) -> Unit,
    onBlogClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LatestFeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && totalItems > 0
        }
    }

    val loadMoreSignal = uiState.loadMoreSignal(shouldLoadMore)

    LaunchedEffect(loadMoreSignal) {
        if (loadMoreSignal.shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                LatestFeedUiEffect.ScrollToTop -> listState.scrollToItem(0)
            }
        }
    }

    val isSticky by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 1 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "최신 글",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                    )
                }
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

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        stickyHeader(key = "filter") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background),
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
                                        label = {
                                            Text(
                                                "안 본 글만",
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (uiState.showUnreadOnly) {
                                                    Icons.Filled.VisibilityOff
                                                } else {
                                                    Icons.Filled.Visibility
                                                },
                                                contentDescription = null,
                                            )
                                        },
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (isSticky) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    )
                                }
                            }
                        }

                        if (uiState.displayArticles.isEmpty() && !uiState.isLoading) {
                            item(key = "empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
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
                            item(key = "loading_more") {
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
