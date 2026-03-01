package org.ikseong.devnews.util

import kotlinx.datetime.Instant

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
        days < 7 -> "${days}일 전"
        days < 30 -> "${days / 7}주 전"
        days < 365 -> "${days / 30}개월 전"
        else -> "${days / 365}년 전"
    }
}
