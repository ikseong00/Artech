package org.ikseong.artech.data.model

import kotlinx.datetime.Instant

fun ArticleDto.toArticle(): Article {
    return Article(
        id = id,
        title = title,
        link = link,
        summary = summary,
        category = category,
        blogSource = blogSource,
        publishedAt = parseInstantOrNull(publishedAt),
        createdAt = parseInstantOrNull(createdAt),
        thumbnailUrl = thumbnailUrl,
    )
}

private fun parseInstantOrNull(dateString: String?): Instant? {
    if (dateString == null) return null
    return try {
        Instant.parse(dateString.normalizeTimestamp())
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun String.normalizeTimestamp(): String {
    return this
        .replace(" ", "T")
        .let { normalized ->
            when {
                normalized.matches(Regex(".*[+-]\\d{2}:\\d{2}$")) -> normalized
                normalized.endsWith("Z") -> normalized
                normalized.matches(Regex(".*[+-]\\d{4}$")) -> normalized.dropLast(2) + ":" + normalized.takeLast(2)
                normalized.matches(Regex(".*[+-]\\d{2}$")) -> "${normalized}:00"
                else -> normalized
            }
        }
}
