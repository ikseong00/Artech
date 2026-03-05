package org.ikseong.artech.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import org.ikseong.artech.data.model.ArticleFeedbackDto
import org.ikseong.artech.data.model.FeedbackReason

class FeedbackRepository(private val client: SupabaseClient) {

    suspend fun submitFeedback(articleId: Long, reason: FeedbackReason) {
        client.from(TABLE_NAME).insert(
            ArticleFeedbackDto(articleId = articleId, reason = reason)
        )
    }

    companion object {
        private const val TABLE_NAME = "article_feedback"
    }
}
