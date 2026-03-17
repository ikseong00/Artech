package org.ikseong.artech.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun ForceUpdateDialog(
    onUpdate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("업데이트 필요") },
        text = { Text("새로운 버전이 출시되었습니다.\n앱을 사용하려면 업데이트가 필요합니다.") },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("업데이트")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
}

@Composable
fun OptionalUpdateDialog(
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("업데이트 안내") },
        text = { Text("새로운 버전이 출시되었습니다.\n업데이트하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("업데이트")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    )
}
