package org.ikseong.devnews.util

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

actual fun shareUrl(url: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(url),
        applicationActivities = null,
    )
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    rootVC.presentViewController(activityVC, animated = true, completion = null)
}
