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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.SpeakerNotes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onSubmit: (FeedbackReason, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedReason by remember { mutableStateOf<FeedbackReason?>(null) }
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
            if (isSubmitting) {
                Text(
                    text = "피드백 보내기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 24.dp),
                )
            } else if (selectedReason == null) {
                Text(
                    text = "피드백 보내기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                val reasonItems = listOf(
                    FeedbackReason.WrongCategory to Icons.Outlined.Label,
                    FeedbackReason.BadSummary to Icons.Outlined.SpeakerNotes,
                    FeedbackReason.DuplicateArticle to Icons.Outlined.ContentCopy,
                    FeedbackReason.WebViewLoadFailure to Icons.Outlined.BrokenImage,
                )
                reasonItems.forEachIndexed { index, (reason, icon) ->
                    FeedbackReasonItem(
                        icon = icon,
                        reason = reason,
                        onClick = { selectedReason = reason },
                    )
                    if (index < reasonItems.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    IconButton(onClick = { selectedReason = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "상세 설명 (선택)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = "선택한 사유: ${selectedReason!!.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onSubmit(selectedReason!!, null) },
                    ) {
                        Text("건너뛰기")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onSubmit(
                                selectedReason!!,
                                description.ifBlank { null },
                            )
                        },
                    ) {
                        Text("전송")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackReasonItem(
    icon: ImageVector,
    reason: FeedbackReason,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
