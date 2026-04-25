package org.ikseong.artech.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ikseong.artech.data.local.dao.FavoriteDao
import org.ikseong.artech.data.local.entity.toArticle
import org.ikseong.artech.data.local.entity.toFavoriteEntity
import org.ikseong.artech.data.local.entity.toSavedFavoriteArticle
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.SavedFavoriteArticle

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    fun getAll(): Flow<List<Article>> =
        favoriteDao.getAll().map { entities -> entities.map { it.toArticle() } }

    fun getAllWithSavedAt(): Flow<List<SavedFavoriteArticle>> =
        favoriteDao.getAll().map { entities -> entities.map { it.toSavedFavoriteArticle() } }

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
