package org.ikseong.artech.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ikseong.artech.data.local.dao.ReadHistoryDao
import org.ikseong.artech.data.local.entity.toHistoryArticle
import org.ikseong.artech.data.local.entity.toReadHistoryEntity
import org.ikseong.artech.data.model.Article
import org.ikseong.artech.data.model.HistoryArticle

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
