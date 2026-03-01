package org.ikseong.devnews.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.devnews.ui.component.ArticleCard
import org.ikseong.devnews.ui.component.EmptyState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onArticleClick: (articleId: Long, link: String) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("읽기이력 전체 삭제") },
            text = { Text("모든 읽기이력을 삭제하시겠습니까?") },
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
                title = { Text("읽기이력") },
                actions = {
                    if (!uiState.isEmpty) {
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
        if (uiState.isEmpty) {
            EmptyState(
                icon = Icons.Filled.History,
                message = "읽기이력이 없습니다",
                description = "아티클을 읽으면 여기에 기록됩니다",
                ctaText = "홈으로 이동",
                onCtaClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.groupedArticles.forEach { group ->
                    item(key = "header_${group.label}") {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                    }
                    items(
                        items = group.articles,
                        key = { it.article.id },
                    ) { historyArticle ->
                        ArticleCard(
                            article = historyArticle.article,
                            onClick = {
                                onArticleClick(
                                    historyArticle.article.id,
                                    historyArticle.article.link,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
