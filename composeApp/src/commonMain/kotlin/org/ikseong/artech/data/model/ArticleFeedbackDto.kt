package org.ikseong.artech.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleFeedbackDto(
    @SerialName("article_id") val articleId: Long,
    val reason: FeedbackReason? = null,
    val description: String? = null,
)
