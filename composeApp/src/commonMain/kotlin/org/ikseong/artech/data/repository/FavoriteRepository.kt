package org.ikseong.devnews.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ikseong.devnews.data.local.dao.FavoriteDao
import org.ikseong.devnews.data.local.entity.toArticle
import org.ikseong.devnews.data.local.entity.toFavoriteEntity
import org.ikseong.devnews.data.model.Article

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    fun getAll(): Flow<List<Article>> =
        favoriteDao.getAll().map { entities -> entities.map { it.toArticle() } }

    fun isFavorite(articleId: Long): Flow<Boolean> =
        favoriteDao.isFavorite(articleId)

    suspend fun toggle(article: Article) {
        val exists = favoriteDao.isFavorite(article.id).first()
        if (exists) {
            favoriteDao.deleteByArticleId(article.id)
        } else {
            favoriteDao.insert(article.toFavoriteEntity())
        }
    }

    suspend fun deleteAll() {
        favoriteDao.deleteAll()
    }
}
