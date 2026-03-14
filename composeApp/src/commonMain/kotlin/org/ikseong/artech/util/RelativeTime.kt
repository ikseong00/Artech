package org.ikseong.artech.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.year}.${local.monthNumber.toString().padStart(2, '0')}.${local.dayOfMonth.toString().padStart(2, '0')}"
}
