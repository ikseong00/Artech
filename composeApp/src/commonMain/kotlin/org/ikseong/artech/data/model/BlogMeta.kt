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

object BlogMetaRegistry {

    private val registry = mapOf(
        "네이버 D2" to BlogMeta(
            name = "네이버 D2",
            url = "https://d2.naver.com",
            logoUrl = faviconUrl("d2.naver.com"),
        ),
        "당근" to BlogMeta(
            name = "당근",
            url = "https://medium.com/daangn",
            logoUrl = faviconUrl("medium.com"),
        ),
        "라인" to BlogMeta(
            name = "라인",
            url = "https://engineering.linecorp.com",
            logoUrl = faviconUrl("engineering.linecorp.com"),
        ),
        "마켓컬리" to BlogMeta(
            name = "마켓컬리",
            url = "https://helloworld.kurly.com",
            logoUrl = faviconUrl("helloworld.kurly.com"),
        ),
        "무신사" to BlogMeta(
            name = "무신사",
            url = "https://medium.com/musinsa-tech",
            logoUrl = faviconUrl("medium.com"),
        ),
        "쏘카" to BlogMeta(
            name = "쏘카",
            url = "https://tech.socarcorp.kr",
            logoUrl = faviconUrl("tech.socarcorp.kr"),
        ),
        "여기어때" to BlogMeta(
            name = "여기어때",
            url = "https://techblog.gccompany.co.kr",
            logoUrl = faviconUrl("techblog.gccompany.co.kr"),
        ),
        "요기요" to BlogMeta(
            name = "요기요",
            url = "https://techblog.yogiyo.co.kr",
            logoUrl = faviconUrl("techblog.yogiyo.co.kr"),
        ),
        "우아한형제들" to BlogMeta(
            name = "우아한형제들",
            url = "https://techblog.woowahan.com",
            logoUrl = faviconUrl("techblog.woowahan.com"),
        ),
        "카카오" to BlogMeta(
            name = "카카오",
            url = "https://tech.kakao.com",
            logoUrl = faviconUrl("tech.kakao.com"),
        ),
        "쿠팡테크" to BlogMeta(
            name = "쿠팡테크",
            url = "https://medium.com/coupang-engineering",
            logoUrl = faviconUrl("medium.com"),
        ),
        "토스" to BlogMeta(
            name = "토스",
            url = "https://toss.tech",
            logoUrl = faviconUrl("toss.tech"),
        ),
        "Airbnb Tech Blog" to BlogMeta(
            name = "Airbnb Tech Blog",
            url = "https://medium.com/airbnb-engineering",
            logoUrl = faviconUrl("airbnb.io"),
        ),
        "Google Developers" to BlogMeta(
            name = "Google Developers",
            url = "https://developers.googleblog.com",
            logoUrl = faviconUrl("developers.googleblog.com"),
        ),
        "Meta Engineering" to BlogMeta(
            name = "Meta Engineering",
            url = "https://engineering.fb.com",
            logoUrl = faviconUrl("engineering.fb.com"),
        ),
        "Netflix Tech Blog" to BlogMeta(
            name = "Netflix Tech Blog",
            url = "https://netflixtechblog.com",
            logoUrl = faviconUrl("netflixtechblog.com"),
        ),
    )

    fun getBlogMeta(blogSource: String): BlogMeta = registry[blogSource]
        ?: BlogMeta(
            name = blogSource,
            url = "",
            logoUrl = "",
        )

    private fun faviconUrl(domain: String): String =
        "https://www.google.com/s2/favicons?domain=$domain&sz=128"
}
