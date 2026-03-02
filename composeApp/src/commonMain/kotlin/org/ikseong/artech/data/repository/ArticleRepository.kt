package org.ikseong.artech.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleCategory
import org.ikseong.artech.data.model.ArticleDto
import org.ikseong.artech.data.model.toArticle

class ArticleRepository(private val client: SupabaseClient) {

    suspend fun getArticles(
        categories: Set<ArticleCategory> = emptySet(),
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    if (categories.isNotEmpty()) {
                        isIn("primary_category", categories.map { it.displayName })
                    } else {
                        neq("primary_category", EXCLUDED_CATEGORY)
                    }
                }
                order("published_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ArticleDto>()
            .map { it.toArticle() }
    }

    suspend fun searchArticles(
        keyword: String,
        categories: Set<ArticleCategory> = emptySet(),
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    if (categories.isNotEmpty()) {
                        isIn("primary_category", categories.map { it.displayName })
                    } else {
                        neq("primary_category", EXCLUDED_CATEGORY)
                    }
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
        private const val EXCLUDED_CATEGORY = "Hiring"
        const val DEFAULT_PAGE_SIZE = 20
    }
}
