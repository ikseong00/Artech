package org.ikseong.devnews.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object PlatformHelper : KoinComponent {
    val context: Context by inject()
}

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    PlatformHelper.context.startActivity(intent)
}

actual fun shareUrl(url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    PlatformHelper.context.startActivity(chooser)
}
