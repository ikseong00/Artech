package org.ikseong.devnews.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import org.ikseong.devnews.data.model.Article
import org.ikseong.devnews.data.model.ArticleCategory
import org.ikseong.devnews.data.model.ArticleDto
import org.ikseong.devnews.data.model.toArticle

class ArticleRepository(private val client: SupabaseClient) {

    suspend fun getArticles(
        category: ArticleCategory? = null,
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                if (category != null) {
                    filter { eq("category", category.displayName) }
                }
                order("published_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ArticleDto>()
            .map { it.toArticle() }
    }

    suspend fun searchArticles(
        keyword: String,
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    or {
                        ilike("title", "%$keyword%")
                        ilike("summary", "%$keyword%")
                    }
                }
                order("published_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ArticleDto>()
            .map { it.toArticle() }
    }

    suspend fun getArticle(id: Long): Article? {
        return client.from(TABLE_NAME)
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<ArticleDto>()
            .firstOrNull()
            ?.toArticle()
    }

    companion object {
        private const val TABLE_NAME = "tech_blog_articles"
        const val DEFAULT_PAGE_SIZE = 20
    }
}
