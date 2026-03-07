package org.ikseong.artech.data.model

import kotlinx.datetime.Instant

data class Article(
    val id: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: String?,
    val blogSource: String,
    val publishedAt: Instant?,
    val createdAt: Instant?,
    val thumbnailUrl: String? = null,
) {
    val displayDate: Instant
        get() = publishedAt ?: createdAt ?: Instant.DISTANT_PAST
}
