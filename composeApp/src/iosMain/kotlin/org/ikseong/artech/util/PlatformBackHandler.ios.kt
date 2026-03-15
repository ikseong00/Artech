package org.ikseong.artech.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS에는 시스템 뒤로가기 버튼이 없으므로 no-op
}

@Composable
actual fun rememberExitAppAction(): () -> Unit {
    // iOS에서는 프로그래밍적 앱 종료가 없으므로 no-op
    return {}
}
