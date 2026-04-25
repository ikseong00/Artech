package org.ikseong.artech.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant

interface VisitSessionStorage {
    val lastVisitTime: Flow<Instant?>

    suspend fun updateLastVisitTime()
}

class VisitSessionRepository(
    private val storage: VisitSessionStorage,
) {
    private val mutex = Mutex()
    private var initialized = false
    private var sessionLastVisitTime: Instant? = null

    suspend fun getSessionLastVisitTime(): Instant? = mutex.withLock {
        if (!initialized) {
            sessionLastVisitTime = storage.lastVisitTime.first()
            storage.updateLastVisitTime()
            initialized = true
        }
        sessionLastVisitTime
    }
}
