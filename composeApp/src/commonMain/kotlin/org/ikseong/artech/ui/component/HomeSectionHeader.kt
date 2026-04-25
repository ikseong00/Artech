package org.ikseong.artech.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    actionLoading: Boolean = false,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )

        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                enabled = actionEnabled && !actionLoading,
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                if (actionLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
