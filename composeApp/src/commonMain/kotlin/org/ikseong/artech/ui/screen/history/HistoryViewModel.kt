package org.ikseong.artech.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ikseong.artech.data.model.HistoryArticle
import org.ikseong.artech.data.repository.HistoryRepository

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState = combine(
        historyRepository.getAllWithReadAt(),
        _isRefreshing,
    ) { articles, isRefreshing ->
        HistoryUiState(
            groupedArticles = groupByDate(articles),
            isRefreshing = isRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500L)
            _isRefreshing.value = false
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            historyRepository.deleteAll()
        }
    }

    private fun groupByDate(articles: List<HistoryArticle>): List<HistoryGroup> {
        if (articles.isEmpty()) return emptyList()

        val timeZone = TimeZone.currentSystemDefault()

        return articles
            .sortedByDescending { it.readAt }
            .groupBy { article ->
                article.readAt.toLocalDateTime(timeZone).date
            }
            .entries
            .sortedByDescending { it.key }
            .map { (date, groupArticles) ->
                HistoryGroup(
                    label = formatDateLabel(date),
                    articles = groupArticles,
                )
            }
    }

    @Suppress("DEPRECATION")
    private fun formatDateLabel(date: LocalDate): String {
        return "${date.monthNumber}월 ${date.dayOfMonth}일"
    }
}
