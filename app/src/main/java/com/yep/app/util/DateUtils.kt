package com.yep.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val storageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun today(): String = storageFormat.format(Date())

    fun todayDisplay(): String = displayFormat.format(Date())

    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

    fun daysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return storageFormat.format(cal.time)
    }

    fun formatHistoryDate(dateStr: String): String {
        return try {
            val date = storageFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}
