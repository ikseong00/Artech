package org.ikseong.artech.data.local.entity

import kotlinx.datetime.Instant
import kotlin.time.Clock
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.HistoryArticle

fun Article.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    articleId = id,
    title = title,
    link = link,
    summary = summary,
    category = category,
    blogSource = blogSource,
    publishedAt = publishedAt?.toEpochMilliseconds(),
    createdAt = createdAt?.toEpochMilliseconds(),
    savedAt = Clock.System.now().toEpochMilliseconds(),
    thumbnailUrl = thumbnailUrl,
)

fun FavoriteEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    link = link,
    summary = summary,
    category = category,
    blogSource = blogSource,
    publishedAt = publishedAt?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = createdAt?.let { Instant.fromEpochMilliseconds(it) },
    thumbnailUrl = thumbnailUrl,
)

fun Article.toReadHistoryEntity(): ReadHistoryEntity = ReadHistoryEntity(
    articleId = id,
    title = title,
    link = link,
    summary = summary,
    category = category,
    blogSource = blogSource,
    publishedAt = publishedAt?.toEpochMilliseconds(),
    createdAt = createdAt?.toEpochMilliseconds(),
    readAt = Clock.System.now().toEpochMilliseconds(),
    thumbnailUrl = thumbnailUrl,
)

fun ReadHistoryEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    link = link,
    summary = summary,
    category = category,
    blogSource = blogSource,
    publishedAt = publishedAt?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = createdAt?.let { Instant.fromEpochMilliseconds(it) },
    thumbnailUrl = thumbnailUrl,
)

fun ReadHistoryEntity.toHistoryArticle(): HistoryArticle = HistoryArticle(
    article = toArticle(),
    readAt = Instant.fromEpochMilliseconds(readAt),
)
