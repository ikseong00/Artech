package org.ikseong.devnews.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ikseong.devnews.data.local.dao.ReadHistoryDao
import org.ikseong.devnews.data.local.entity.toHistoryArticle
import org.ikseong.devnews.data.local.entity.toReadHistoryEntity
import org.ikseong.devnews.data.model.Article
import org.ikseong.devnews.data.model.HistoryArticle

class HistoryRepository(private val readHistoryDao: ReadHistoryDao) {

    fun getAllWithReadAt(): Flow<List<HistoryArticle>> =
        readHistoryDao.getAll().map { entities -> entities.map { it.toHistoryArticle() } }

    suspend fun record(article: Article) {
        readHistoryDao.upsert(article.toReadHistoryEntity())
    }

    suspend fun deleteAll() {
        readHistoryDao.deleteAll()
    }
}
