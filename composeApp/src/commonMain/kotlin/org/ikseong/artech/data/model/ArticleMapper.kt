package org.ikseong.devnews.data.model

import kotlinx.datetime.Instant

fun ArticleDto.toArticle(): Article {
    val displayDate = parseInstantOrNull(publishedAt)
        ?: parseInstantOrNull(createdAt)
        ?: Instant.DISTANT_PAST

    return Article(
        id = id,
        title = title,
        link = link,
        summary = summary,
        category = category,
        blogSource = blogSource,
        displayDate = displayDate,
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
