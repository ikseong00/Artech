package org.ikseong.artech.data.model

data class BlogMeta(
    val name: String,
    val url: String,
    val logoUrl: String,
)

data class BlogStats(
    val totalCount: Int,
    val earliestDate: String?,
    val latestDate: String?,
)

fun faviconUrl(domain: String): String =
    "https://www.google.com/s2/favicons?domain=$domain&sz=128"
