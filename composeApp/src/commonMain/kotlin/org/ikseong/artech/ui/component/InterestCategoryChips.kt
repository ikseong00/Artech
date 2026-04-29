package org.ikseong.artech.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun InterestCategoryChips(
    categories: List<String>,
    selectedCategories: List<String>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val selectedCategorySet = selectedCategories.toSet()
    val selectedCategoryList = selectedCategories
        .distinct()
        .filter { it.isNotBlank() }
    val unselectedCategories = categories.filterNot { it in selectedCategorySet }

    Column(modifier = modifier.fillMaxWidth()) {
        if (selectedCategoryList.isNotEmpty()) {
            SelectedCategoryTray(
                selectedCategories = selectedCategoryList,
                onCategoryClick = onCategoryClick,
                contentPadding = contentPadding,
            )
            if (unselectedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (unselectedCategories.isNotEmpty()) {
            LazyRow(
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = unselectedCategories,
                    key = { it },
                ) { category ->
                    AddCategoryChip(
                        category = category,
                        onClick = { onCategoryClick(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedCategoryTray(
    selectedCategories: List<String>,
    onCategoryClick: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    Column {
        Text(
            text = "선택됨 ${selectedCategories.size}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
        )
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = selectedCategories,
                key = { "selected_$it" },
            ) { category ->
                SelectedCategoryChip(
                    category = category,
                    onRemoveClick = { onCategoryClick(category) },
                )
            }
        }
    }
}

@Composable
private fun SelectedCategoryChip(
    category: String,
    onRemoveClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.88f),
        ),
        modifier = Modifier
            .height(36.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onRemoveClick),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 132.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "$category 선택 해제",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AddCategoryChip(
    category: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.88f),
        ),
        modifier = Modifier
            .height(36.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 132.dp),
            )
        }
    }
}
