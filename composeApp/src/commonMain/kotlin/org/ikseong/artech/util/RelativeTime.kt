package org.ikseong.artech.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.year}.${local.monthNumber.toString().padStart(2, '0')}.${local.dayOfMonth.toString().padStart(2, '0')}"
}

fun relativeTimeString(instant: Instant): String {
    val now = kotlin.time.Clock.System.now()
    val durationMs = (now.toEpochMilliseconds() - instant.toEpochMilliseconds())

    val minutes = durationMs / 60_000
    val hours = durationMs / 3_600_000
    val days = durationMs / 86_400_000

    return when {
        minutes < 1 -> "방금 전"
        hours < 1 -> "${minutes}분 전"
        days < 1 -> "${hours}시간 전"
        days <= 10 -> "${days}일 전"
        else -> formatDate(instant)
    }
}
