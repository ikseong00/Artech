package org.ikseong.devnews.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ikseong.devnews.data.model.ArticleCategory

@Composable
fun CategoryFilterRow(
    selectedCategory: ArticleCategory?,
    onCategorySelected: (ArticleCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(null) + ArticleCategory.entries

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories) { category ->
            val selected = selectedCategory == category

            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category?.displayName ?: "전체",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
