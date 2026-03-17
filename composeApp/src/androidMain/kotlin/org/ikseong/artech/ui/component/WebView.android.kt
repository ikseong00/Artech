package org.ikseong.artech.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private class ScrollAwareWebView(
    context: Context,
    private val onScrollDirectionChanged: ((ScrollDirection) -> Unit)?,
) : android.webkit.WebView(context) {

    private var accumulatedDelta = 0
    private var lastReportedDirection = ScrollDirection.NONE

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val callback = onScrollDirectionChanged ?: return
        val dy = t - oldt
        if (dy == 0) return

        if ((accumulatedDelta > 0 && dy < 0) || (accumulatedDelta < 0 && dy > 0)) {
            accumulatedDelta = 0
        }

        accumulatedDelta += dy

        val hideThreshold = 150
        val showThreshold = 300
        if (accumulatedDelta > hideThreshold && lastReportedDirection != ScrollDirection.DOWN) {
            lastReportedDirection = ScrollDirection.DOWN
            callback(ScrollDirection.DOWN)
        } else if (accumulatedDelta < -showThreshold && lastReportedDirection != ScrollDirection.UP) {
            lastReportedDirection = ScrollDirection.UP
            callback(ScrollDirection.UP)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onScrollDirectionChanged: ((ScrollDirection) -> Unit)?,
) {
    AndroidView(
        factory = { context ->
            ScrollAwareWebView(context, onScrollDirectionChanged).apply {
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.userAgentString = settings.userAgentString.replace(
                    Regex("\\bwv\\b"), "",
                ).replace("  ", " ")
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = modifier,
    )
}
