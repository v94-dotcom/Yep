package com.yep.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.Item
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryDay(
    val date: String,
    val confirmedItemIds: Set<String>,
    val totalItems: Int
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: YepRepository
) : ViewModel() {

    private val startDate = DateUtils.daysAgo(29)

    val items: StateFlow<List<Item>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // historyDays[0] = today, [1] = yesterday, ..., [29] = 29 days ago
    val historyDays: StateFlow<List<HistoryDay>> = combine(
        repository.getAllItems(),
        repository.getConfirmationsSince(startDate)
    ) { itemsList, confirmations ->
        val totalItems = itemsList.size
        val confirmsByDate = confirmations.groupBy { it.date }
        (0 until 30).map { daysBack ->
            val date = DateUtils.daysAgo(daysBack)
            val dayConfirms = confirmsByDate[date] ?: emptyList()
            HistoryDay(
                date = date,
                confirmedItemIds = dayConfirms.map { it.itemId }.toSet(),
                totalItems = totalItems
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
