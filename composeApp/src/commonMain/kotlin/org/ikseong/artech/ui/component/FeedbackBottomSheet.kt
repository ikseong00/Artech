package org.ikseong.artech.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.SpeakerNotes
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ikseong.artech.data.model.FeedbackReason

private const val MAX_DESCRIPTION_LENGTH = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    isSubmitting: Boolean,
    onSubmit: (Set<FeedbackReason>, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedReasons by remember { mutableStateOf(emptySet<FeedbackReason>()) }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "피드백 보내기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 24.dp),
                )
            } else {
                val reasonItems = listOf(
                    FeedbackReason.WrongCategory to Icons.Outlined.Label,
                    FeedbackReason.BadSummary to Icons.Outlined.SpeakerNotes,
                    FeedbackReason.DuplicateArticle to Icons.Outlined.ContentCopy,
                    FeedbackReason.WebViewLoadFailure to Icons.Outlined.BrokenImage,
                    FeedbackReason.ThumbnailError to Icons.Outlined.HideImage,
                )
                reasonItems.forEachIndexed { index, (reason, icon) ->
                    FeedbackReasonItem(
                        icon = icon,
                        reason = reason,
                        isChecked = reason in selectedReasons,
                        onClick = {
                            selectedReasons = if (reason in selectedReasons) {
                                selectedReasons - reason
                            } else {
                                selectedReasons + reason
                            }
                        },
                    )
                    if (index < reasonItems.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= MAX_DESCRIPTION_LENGTH) description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("추가 설명을 입력해주세요") },
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text(
                            text = "${description.length}/$MAX_DESCRIPTION_LENGTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmit(
                            selectedReasons,
                            description.ifBlank { null },
                        )
                    },
                    enabled = selectedReasons.isNotEmpty() || description.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = "전송",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackReasonItem(
    icon: ImageVector,
    reason: FeedbackReason,
    isChecked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onClick() },
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = reason.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
