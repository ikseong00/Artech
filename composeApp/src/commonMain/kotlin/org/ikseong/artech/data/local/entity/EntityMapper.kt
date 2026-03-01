package org.ikseong.artech.data.local.entity

import kotlinx.datetime.Instant
import kotlin.time.Clock
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory
import org.ikseong.artech.data.model.HistoryArticle

fun Article.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    articleId = id,
    title = title,
    link = link,
    summary = summary,
    category = category?.name,
    blogSource = blogSource,
    displayDate = displayDate.toEpochMilliseconds(),
    savedAt = Clock.System.now().toEpochMilliseconds(),
)

fun FavoriteEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    link = link,
    summary = summary,
    category = category?.let { name ->
        ArticleCategory.entries.find { it.name == name }
    },
    blogSource = blogSource,
    displayDate = Instant.fromEpochMilliseconds(displayDate),
)

fun Article.toReadHistoryEntity(): ReadHistoryEntity = ReadHistoryEntity(
    articleId = id,
    title = title,
    link = link,
    summary = summary,
    category = category?.name,
    blogSource = blogSource,
    displayDate = displayDate.toEpochMilliseconds(),
    readAt = Clock.System.now().toEpochMilliseconds(),
)

fun ReadHistoryEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    link = link,
    summary = summary,
    category = category?.let { name ->
        ArticleCategory.entries.find { it.name == name }
    },
    blogSource = blogSource,
    displayDate = Instant.fromEpochMilliseconds(displayDate),
)

fun ReadHistoryEntity.toHistoryArticle(): HistoryArticle = HistoryArticle(
    article = toArticle(),
    readAt = Instant.fromEpochMilliseconds(readAt),
)
