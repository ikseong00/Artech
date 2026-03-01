package org.ikseong.devnews.ui.screen.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.devnews.ui.component.WebView
import org.ikseong.devnews.util.openUrl
import org.ikseong.devnews.util.shareUrl
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = { openUrl(viewModel.link) }) {
                        Icon(
                            imageVector = Icons.Filled.OpenInBrowser,
                            contentDescription = "외부 브라우저로 열기",
                        )
                    }
                    IconButton(onClick = { shareUrl(viewModel.link) }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "공유",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        WebView(
            url = viewModel.link,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
