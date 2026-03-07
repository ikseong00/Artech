package org.ikseong.artech.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterRow(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<String> = emptyList(),
) {
    var isExpanded by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier,
    ) { expanded ->
        if (expanded) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    AllChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                    )
                    categories.forEach { category ->
                        CategoryChip(
                            category = category,
                            selected = category == selectedCategory,
                            onClick = {
                                onCategorySelected(
                                    if (category == selectedCategory) null else category,
                                )
                            },
                        )
                    }
                }
                IconButton(onClick = { isExpanded = false }) {
                    Icon(
                        imageVector = Icons.Filled.ExpandLess,
                        contentDescription = "접기",
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "all") {
                        AllChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                        )
                    }
                    items(
                        items = categories,
                        key = { it },
                    ) { category ->
                        CategoryChip(
                            category = category,
                            selected = category == selectedCategory,
                            onClick = {
                                onCategorySelected(
                                    if (category == selectedCategory) null else category,
                                )
                            },
                        )
                    }
                }
                IconButton(onClick = { isExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "펼치기",
                    )
                }
            }
        }
    }
}

@Composable
private fun AllChip(
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "전체",
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun CategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}
