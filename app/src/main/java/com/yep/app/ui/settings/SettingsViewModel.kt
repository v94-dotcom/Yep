package com.yep.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.UserSettings
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: YepRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _reminderEnabled = MutableStateFlow(false)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled

    private val _reminderTime = MutableStateFlow("08:30")
    val reminderTime: StateFlow<String> = _reminderTime

    init {
        viewModelScope.launch {
            val settings = repository.getSettings().first() ?: UserSettings()
            _reminderEnabled.value = settings.reminderEnabled
            _reminderTime.value = settings.reminderTime
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
        viewModelScope.launch {
            val current = repository.getSettings().first() ?: UserSettings()
            repository.saveSettings(current.copy(reminderEnabled = enabled))
            if (enabled) {
                NotificationHelper.scheduleReminder(context, _reminderTime.value)
            } else {
                NotificationHelper.cancelReminder(context)
            }
        }
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
        viewModelScope.launch {
            val current = repository.getSettings().first() ?: UserSettings()
            repository.saveSettings(current.copy(reminderTime = time))
            NotificationHelper.cancelReminder(context)
            NotificationHelper.scheduleReminder(context, time)
        }
    }
}
