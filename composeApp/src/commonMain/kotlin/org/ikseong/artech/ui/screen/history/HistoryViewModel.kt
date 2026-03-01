package org.ikseong.devnews.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ikseong.devnews.data.model.HistoryArticle
import org.ikseong.devnews.data.repository.HistoryRepository

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val uiState = historyRepository.getAllWithReadAt()
        .map { articles -> HistoryUiState(groupedArticles = groupByDate(articles)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun deleteAll() {
        viewModelScope.launch {
            historyRepository.deleteAll()
        }
    }

    private fun groupByDate(articles: List<HistoryArticle>): List<HistoryGroup> {
        if (articles.isEmpty()) return emptyList()

        val timeZone = TimeZone.currentSystemDefault()
        val now = kotlin.time.Clock.System.now()
        val today = Instant.fromEpochMilliseconds(now.toEpochMilliseconds())
            .toLocalDateTime(timeZone).date

        val groups = mutableMapOf<String, MutableList<HistoryArticle>>()

        for (article in articles) {
            val label = dateGroupLabel(article.readAt, today, timeZone)
            groups.getOrPut(label) { mutableListOf() }.add(article)
        }

        val order = listOf("오늘", "어제", "이번 주", "이전")
        return order.mapNotNull { label ->
            groups[label]?.let { HistoryGroup(label = label, articles = it) }
        }
    }

    private fun dateGroupLabel(
        readAt: Instant,
        today: LocalDate,
        timeZone: TimeZone,
    ): String {
        val readDate = readAt.toLocalDateTime(timeZone).date
        val daysDiff = today.toEpochDays().toLong() - readDate.toEpochDays().toLong()

        return when {
            daysDiff == 0L -> "오늘"
            daysDiff == 1L -> "어제"
            daysDiff < 7L -> "이번 주"
            else -> "이전"
        }
    }
}
