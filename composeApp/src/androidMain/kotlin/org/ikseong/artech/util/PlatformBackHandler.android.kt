package org.ikseong.artech.util

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberExitAppAction(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { (context as? Activity)?.finishAffinity() }
    }
}
