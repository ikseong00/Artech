package org.ikseong.artech.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class VisitSessionRepositoryTest {

    @Test
    fun getSessionLastVisitTime_pins_initial_baseline_after_visit_is_marked() = runBlocking {
        val initialLastVisit = Instant.parse("2026-04-25T01:00:00Z")
        val storage = FakeVisitSessionStorage(initialLastVisit)
        val repository = VisitSessionRepository(storage)

        assertEquals(initialLastVisit, repository.getSessionLastVisitTime())
        assertEquals(1, storage.updateCount)
        assertEquals(initialLastVisit, repository.getSessionLastVisitTime())
        assertEquals(1, storage.updateCount)
    }

    private class FakeVisitSessionStorage(
        initialLastVisit: Instant?,
    ) : VisitSessionStorage {
        override val lastVisitTime = MutableStateFlow(initialLastVisit)
        var updateCount = 0
            private set

        override suspend fun updateLastVisitTime() {
            updateCount++
            lastVisitTime.value = Instant.parse("2026-04-25T02:00:00Z")
        }
    }
}
