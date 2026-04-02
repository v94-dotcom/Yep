package com.yep.app.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.Item
import com.yep.app.data.entities.UserSettings
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: YepRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // null = loading, false = needs onboarding, true = done
    val onboardingComplete: StateFlow<Boolean?> = repository.getSettings()
        .map { settings -> settings?.onboardingComplete ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedLabels = MutableStateFlow(SUGGESTIONS.toSet())
    val selectedLabels: StateFlow<Set<String>> = _selectedLabels.asStateFlow()

    private val _customText = MutableStateFlow("")
    val customText: StateFlow<String> = _customText.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(false)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderTime = MutableStateFlow("08:30")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    fun toggleSuggestion(label: String) {
        _selectedLabels.update { if (label in it) it - label else it + label }
    }

    fun setCustomText(text: String) {
        _customText.value = text
    }

    fun setReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val selected = SUGGESTIONS.filter { it in _selectedLabels.value }.toMutableList()
            val custom = _customText.value.trim()
            if (custom.isNotEmpty()) selected.add(custom)
            val finalLabels = if (selected.isEmpty()) listOf("Locked the door") else selected
            finalLabels.forEachIndexed { i, label ->
                repository.insertItem(Item(label = label, sortOrder = i))
            }
            // Insert items BEFORE marking complete so a process kill leaves things consistent
            repository.saveSettings(
                UserSettings(
                    onboardingComplete = true,
                    reminderEnabled = _reminderEnabled.value,
                    reminderTime = _reminderTime.value
                )
            )
            if (_reminderEnabled.value) {
                NotificationHelper.scheduleReminder(context, _reminderTime.value)
            }
        }
    }

    companion object {
        val SUGGESTIONS = listOf(
            "Locked the door",
            "Stove off",
            "Unplugged iron",
            "Took medication",
            "Closed windows",
            "Turned off lights",
            "Fed the pet"
        )
    }
}
