package org.ikseong.devnews.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            WKWebView().apply {
                NSURL.URLWithString(url)?.let { nsUrl ->
                    loadRequest(NSURLRequest.requestWithURL(nsUrl))
                }
            }
        },
        update = { webView ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
        },
        modifier = modifier,
    )
}
