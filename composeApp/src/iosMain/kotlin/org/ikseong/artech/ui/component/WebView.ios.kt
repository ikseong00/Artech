package org.ikseong.artech.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIScrollView
import platform.WebKit.WKWebView
import platform.darwin.NSObject
import platform.UIKit.UIScrollViewDelegateProtocol

private class ScrollDirectionDelegate : NSObject(), UIScrollViewDelegateProtocol {
    var onScrollDirectionChanged: ((ScrollDirection) -> Unit)? = null

    private var lastContentOffsetY = 0.0
    private var accumulatedDelta = 0.0
    private var lastReportedDirection = ScrollDirection.NONE

    @OptIn(ExperimentalForeignApi::class)
    override fun scrollViewDidScroll(scrollView: UIScrollView) {
        val currentY = scrollView.contentOffset.useContents { y }
        val dy = currentY - lastContentOffsetY
        lastContentOffsetY = currentY

        if (dy == 0.0) return

        if ((accumulatedDelta > 0.0 && dy < 0.0) || (accumulatedDelta < 0.0 && dy > 0.0)) {
            accumulatedDelta = 0.0
        }

        accumulatedDelta += dy

        val hideThreshold = 150.0
        val showThreshold = 300.0
        if (accumulatedDelta > hideThreshold && lastReportedDirection != ScrollDirection.DOWN) {
            lastReportedDirection = ScrollDirection.DOWN
            onScrollDirectionChanged?.invoke(ScrollDirection.DOWN)
        } else if (accumulatedDelta < -showThreshold && lastReportedDirection != ScrollDirection.UP) {
            lastReportedDirection = ScrollDirection.UP
            onScrollDirectionChanged?.invoke(ScrollDirection.UP)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onScrollDirectionChanged: ((ScrollDirection) -> Unit)?,
) {
    val scrollDelegate = remember { ScrollDirectionDelegate() }
    scrollDelegate.onScrollDirectionChanged = onScrollDirectionChanged

    UIKitView(
        factory = {
            WKWebView().apply {
                scrollView.delegate = scrollDelegate
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
