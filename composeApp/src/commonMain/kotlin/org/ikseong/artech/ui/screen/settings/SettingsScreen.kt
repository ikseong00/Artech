package org.ikseong.devnews.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.ikseong.devnews.data.model.ThemeMode
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteFavoritesDialog by remember { mutableStateOf(false) }
    var showDeleteHistoryDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = uiState.themeMode,
            onSelect = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showDeleteFavoritesDialog) {
        DeleteConfirmDialog(
            title = "즐겨찾기 전체 삭제",
            message = "모든 즐겨찾기를 삭제하시겠습니까?",
            onConfirm = {
                viewModel.deleteAllFavorites()
                showDeleteFavoritesDialog = false
            },
            onDismiss = { showDeleteFavoritesDialog = false },
        )
    }

    if (showDeleteHistoryDialog) {
        DeleteConfirmDialog(
            title = "읽기이력 전체 삭제",
            message = "모든 읽기이력을 삭제하시겠습니까?",
            onConfirm = {
                viewModel.deleteAllHistory()
                showDeleteHistoryDialog = false
            },
            onDismiss = { showDeleteHistoryDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("설정") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader(title = "화면 설정")
            SettingsItem(
                icon = Icons.Filled.DarkMode,
                title = "다크모드",
                description = uiState.themeMode.displayName(),
                onClick = { showThemeDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader(title = "데이터 관리")
            SettingsItem(
                icon = Icons.Filled.Bookmark,
                title = "즐겨찾기 전체 삭제",
                description = "저장된 모든 즐겨찾기를 삭제합니다",
                onClick = { showDeleteFavoritesDialog = true },
            )
            SettingsItem(
                icon = Icons.Filled.DeleteSweep,
                title = "읽기이력 전체 삭제",
                description = "저장된 모든 읽기이력을 삭제합니다",
                onClick = { showDeleteHistoryDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader(title = "앱 정보")
            SettingsItem(
                icon = Icons.Filled.Info,
                title = "앱 버전",
                description = uiState.appVersion,
                onClick = null,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentYear = remember {
                val now = kotlin.time.Clock.System.now()
                Instant.fromEpochMilliseconds(now.toEpochMilliseconds())
                    .toLocalDateTime(TimeZone.currentSystemDefault()).year
            }
            Text(
                text = "\u00A9 $currentYear DevNews. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("다크모드 설정") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == currentMode,
                                onClick = { onSelect(mode) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = null,
                        )
                        Text(
                            text = mode.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.SYSTEM -> "시스템 설정"
    ThemeMode.LIGHT -> "라이트 모드"
    ThemeMode.DARK -> "다크 모드"
}
