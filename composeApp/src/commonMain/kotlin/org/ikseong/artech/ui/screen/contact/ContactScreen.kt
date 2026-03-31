package org.ikseong.artech.ui.screen.contact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.ikseong.artech.BuildKonfig
import org.ikseong.artech.ui.component.ScrollDirection
import org.ikseong.artech.ui.component.WebView

private const val CONTACT_FORM_URL = "https://easy-contact-lemon.vercel.app/f/oap4r"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onBack: () -> Unit,
) {
    var isHeaderVisible by remember { mutableStateOf(true) }
    val url = remember { "$CONTACT_FORM_URL?app_version=${BuildKonfig.APP_VERSION}" }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                TopAppBar(
                    title = { Text("문의하기") },
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
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            WebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
                onScrollDirectionChanged = { direction ->
                    when (direction) {
                        ScrollDirection.DOWN -> isHeaderVisible = false
                        ScrollDirection.UP -> isHeaderVisible = true
                        ScrollDirection.NONE -> {}
                    }
                },
            )
        }
    }
}
