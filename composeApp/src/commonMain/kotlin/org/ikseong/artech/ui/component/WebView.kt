package org.ikseong.artech.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebView(
    url: String,
    modifier: Modifier = Modifier,
)
