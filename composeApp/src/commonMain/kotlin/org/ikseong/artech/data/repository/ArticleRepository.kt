package org.ikseong.artech.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.ArticleDto
import org.ikseong.artech.data.model.BlogMeta
import org.ikseong.artech.data.model.BlogStats
import org.ikseong.artech.data.model.CategoryGroup
import org.ikseong.artech.data.model.faviconUrl
import org.ikseong.artech.data.model.toArticle
import io.github.jan.supabase.postgrest.query.Columns

class ArticleRepository(private val client: SupabaseClient) {

    suspend fun getCategories(): List<String> {
        val raw = client.postgrest.rpc("get_distinct_categories")
            .decodeList<CategoryResult>()
            .mapNotNull { it.primaryCategory }
            .filter { it != EXCLUDED_CATEGORY }
        return CategoryGroup.mergeCategories(raw)
    }

    suspend fun getArticles(
        category: String? = null,
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    if (category != null) {
                        val expanded = CategoryGroup.expand(category)
                        isIn("primary_category", expanded)
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
        category: String? = null,
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    if (category != null) {
                        val expanded = CategoryGroup.expand(category)
                        isIn("primary_category", expanded)
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

    suspend fun getArticlesByBlog(
        blogSource: String,
        category: String? = null,
        offset: Int = 0,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): List<Article> {
        return client.from(TABLE_NAME)
            .select {
                filter {
                    eq("blog_source", blogSource)
                    if (category != null) {
                        val expanded = CategoryGroup.expand(category)
                        isIn("primary_category", expanded)
                    }
                }
                order("published_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }
            .decodeList<ArticleDto>()
            .map { it.toArticle() }
    }

    suspend fun getCategoriesByBlog(blogSource: String): List<String> {
        val raw = client.from(TABLE_NAME)
            .select(columns = Columns.list("primary_category")) {
                filter { eq("blog_source", blogSource) }
            }
            .decodeList<CategoryResult>()
            .mapNotNull { it.primaryCategory }
            .distinct()
            .filter { it != EXCLUDED_CATEGORY }
        return CategoryGroup.mergeCategories(raw).sorted()
    }

    suspend fun getBlogStats(blogSource: String): BlogStats {
        val allArticles = client.from(TABLE_NAME)
            .select(columns = Columns.list("published_at", "created_at")) {
                filter { eq("blog_source", blogSource) }
                order("published_at", Order.ASCENDING)
            }
            .decodeList<BlogStatDto>()

        val dates = allArticles.mapNotNull { it.publishedAt ?: it.createdAt }

        return BlogStats(
            totalCount = allArticles.size,
            earliestDate = dates.firstOrNull()?.take(10)?.replace("-", "."),
            latestDate = dates.lastOrNull()?.take(10)?.replace("-", "."),
        )
    }

    suspend fun getAllBlogArticleCounts(): List<BlogArticleCountResult> {
        return client.postgrest.rpc("get_blog_article_counts")
            .decodeList<BlogArticleCountResult>()
    }

    suspend fun getBlogMeta(blogSource: String): BlogMeta {
        val result = client.from(BLOG_SOURCES_TABLE)
            .select {
                filter { eq("name", blogSource) }
                limit(1)
            }
            .decodeList<BlogSourceDto>()
            .firstOrNull()

        return if (result != null) {
            BlogMeta(
                name = result.name,
                url = result.url,
                logoUrl = faviconUrl(result.domain),
            )
        } else {
            BlogMeta(
                name = blogSource,
                url = "",
                logoUrl = "",
            )
        }
    }

    @Serializable
    data class BlogArticleCountResult(
        @SerialName("blog_source")
        val blogSource: String,
        val count: Int,
        val url: String = "",
        val domain: String = "",
    )

    @Serializable
    private data class BlogSourceDto(
        val name: String,
        val url: String = "",
        val domain: String = "",
    )

    @Serializable
    private data class BlogStatDto(
        @SerialName("published_at")
        val publishedAt: String? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
    )

    @Serializable
    private data class CategoryResult(
        @SerialName("primary_category")
        val primaryCategory: String? = null,
    )

    companion object {
        private const val TABLE_NAME = "tech_blog_articles"
        private const val BLOG_SOURCES_TABLE = "blog_sources"
        private const val EXCLUDED_CATEGORY = "Hiring"
        const val DEFAULT_PAGE_SIZE = 20
    }
}
