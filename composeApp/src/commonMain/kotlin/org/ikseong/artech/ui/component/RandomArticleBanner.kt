package org.ikseong.artech.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.CategoryGroup

@Composable
fun RandomArticleBanner(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
) {
    val hasImage = !article.thumbnailUrl.isNullOrBlank()
    val aiSummary = article.summary?.takeIf { it.isNotBlank() }
    val foregroundColor = if (hasImage) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
    val metadataColor = if (hasImage) {
        Color.White.copy(alpha = 0.84f)
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f)
    }
    val categoryContainerColor = if (hasImage) {
        Color.White.copy(alpha = 0.94f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val categoryContentColor = if (hasImage) {
        Color(0xFF121722)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val titleStyle = if (hasImage) {
        MaterialTheme.typography.titleLarge.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.75f),
                offset = Offset(0f, 2f),
                blurRadius = 8f,
            ),
        )
    } else {
        MaterialTheme.typography.titleLarge
    }
    val metadataStyle = if (hasImage) {
        MaterialTheme.typography.labelMedium.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.72f),
                offset = Offset(0f, 1.5f),
                blurRadius = 6f,
            ),
        )
    } else {
        MaterialTheme.typography.labelMedium
    }
    val summaryStyle = if (hasImage) {
        MaterialTheme.typography.bodySmall.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.70f),
                offset = Offset(0f, 1.5f),
                blurRadius = 6f,
            ),
        )
    } else {
        MaterialTheme.typography.bodySmall
    }
    val displayCategory = article.category
        ?.takeIf { it.isNotBlank() }
        ?.let(CategoryGroup::toDisplayName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            if (hasImage) {
                AsyncImage(
                    model = article.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.34f),
                                    Color.Black.copy(alpha = 0.90f),
                                ),
                            ),
                        ),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "지금 읽어볼 글",
                        style = metadataStyle,
                        fontWeight = FontWeight.SemiBold,
                        color = metadataColor,
                    )
                    if (isNew) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = titleStyle,
                    fontWeight = FontWeight.Bold,
                    color = foregroundColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (aiSummary != null) {
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "AI 요약 · $aiSummary",
                        style = summaryStyle,
                        color = metadataColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (displayCategory != null) {
                        Text(
                            text = displayCategory,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .sizeIn(maxWidth = 136.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(categoryContainerColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = article.blogSource,
                        style = MaterialTheme.typography.labelMedium,
                        color = metadataColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
