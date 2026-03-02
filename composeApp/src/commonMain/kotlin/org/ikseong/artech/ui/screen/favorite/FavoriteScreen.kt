package org.ikseong.artech.ui.screen.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.artech.ui.component.ArticleCard
import org.ikseong.artech.ui.component.CategoryFilterRow
import org.ikseong.artech.ui.component.EmptyState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onArticleClick: (articleId: Long, link: String) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: FavoriteViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedCategories) {
        listState.scrollToItem(0)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("스크랩 전체 삭제") },
            text = { Text("모든 스크랩을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        showDeleteDialog = false
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스크랩") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    if (uiState.allArticles.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "전체 삭제",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.allArticles.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.BookmarkBorder,
                message = "스크랩한 아티클이 없습니다",
                description = "관심 있는 아티클을 스크랩해보세요",
                ctaText = "홈으로 이동",
                onCtaClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "category_filter") {
                    CategoryFilterRow(
                        selectedCategories = uiState.selectedCategories,
                        onCategoryToggled = viewModel::toggleCategory,
                        onClearAll = viewModel::clearCategoryFilter,
                    )
                }

                items(
                    items = uiState.articles,
                    key = { it.id },
                ) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id, article.link) },
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(article) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
