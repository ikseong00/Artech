package org.ikseong.artech.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FeedbackReason(val displayName: String) {
    @SerialName("wrong_category")
    WrongCategory("잘못된 카테고리"),

    @SerialName("bad_summary")
    BadSummary("부정확한 AI 요약"),

    @SerialName("duplicate_article")
    DuplicateArticle("중복 게시글"),

    @SerialName("webview_load_failure")
    WebViewLoadFailure("페이지 로딩 실패"),

    @SerialName("thumbnail_error")
    ThumbnailError("썸네일 오류"),
}
