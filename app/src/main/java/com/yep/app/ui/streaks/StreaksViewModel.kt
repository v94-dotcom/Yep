package com.yep.app.ui.streaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.entities.Item
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class StreaksUiState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completionRate: Int = 0,
    val weekDots: List<Boolean> = List(7) { false }
)

@HiltViewModel
class StreaksViewModel @Inject constructor(
    private val repository: YepRepository
) : ViewModel() {

    val uiState: StateFlow<StreaksUiState> = combine(
        repository.getAllItems(),
        repository.getConfirmationsSince(DateUtils.daysAgo(89))
    ) { items, confirmations ->
        if (items.isEmpty()) StreaksUiState() else computeStats(items, confirmations)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreaksUiState())

    private fun computeStats(items: List<Item>, confirmations: List<Confirmation>): StreaksUiState {
        val itemCount = items.size
        val confirmsByDate = confirmations.groupBy { it.date }

        fun isDayComplete(date: String): Boolean {
            val confirmed = confirmsByDate[date]?.map { it.itemId }?.toSet() ?: emptySet()
            return confirmed.size >= itemCount
        }

        // Current streak: consecutive complete days going back.
        // Today counts if complete; either way don't penalise today if still in progress.
        val today = DateUtils.today()
        var currentStreak = 0
        var dayBack = if (isDayComplete(today)) 0 else 1
        while (true) {
            val date = DateUtils.daysAgo(dayBack)
            if (isDayComplete(date)) {
                currentStreak++
                dayBack++
            } else break
            if (dayBack > 89) break
        }

        // Best streak over last 90 days (oldest → newest)
        val dates90 = (89 downTo 0).map { DateUtils.daysAgo(it) }
        var bestStreak = 0
        var run = 0
        for (date in dates90) {
            if (isDayComplete(date)) {
                run++
                if (run > bestStreak) bestStreak = run
            } else {
                run = 0
            }
        }
        bestStreak = maxOf(bestStreak, currentStreak)

        // 30-day completion rate (last 30 days, excluding today)
        val past30 = (1..30).map { DateUtils.daysAgo(it) }
        val completeDays = past30.count { isDayComplete(it) }
        val completionRate = completeDays * 100 / past30.size

        // Weekly dots M–S (index 0 = Mon, 6 = Sun)
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sun=1 … Sat=7
        val daysSinceMonday = (dayOfWeek + 5) % 7      // Mon=0 … Sun=6
        val weekDots = (0 until 7).map { dotIndex ->
            if (dotIndex <= daysSinceMonday) {
                val daysBack = daysSinceMonday - dotIndex
                isDayComplete(DateUtils.daysAgo(daysBack))
            } else false
        }

        return StreaksUiState(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completionRate = completionRate,
            weekDots = weekDots
        )
    }
}
