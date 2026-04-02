package com.yep.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.entities.Item
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: YepRepository
) : ViewModel() {

    val today: String = DateUtils.today()

    val items: StateFlow<List<Item>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayConfirmations: StateFlow<List<Confirmation>> =
        repository.getConfirmationsForDate(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _expandedItemId = MutableStateFlow<String?>(null)
    val expandedItemId: StateFlow<String?> = _expandedItemId.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _shakeEditHeader = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val shakeEditHeader: SharedFlow<Unit> = _shakeEditHeader

    fun requestShakeHeader() {
        _shakeEditHeader.tryEmit(Unit)
    }

    init {
        viewModelScope.launch { seedDefaultsIfNeeded() }
    }

    fun toggleExpand(itemId: String) {
        _expandedItemId.value = if (_expandedItemId.value == itemId) null else itemId
    }

    fun collapseAll() {
        _expandedItemId.value = null
    }

    fun toggleEditMode() {
        _expandedItemId.value = null
        _isEditMode.value = !_isEditMode.value
    }

    fun addItem(label: String) {
        viewModelScope.launch {
            val currentItems = items.value
            repository.insertItem(Item(label = label.trim(), sortOrder = currentItems.size))
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    suspend fun reorderItems(reorderedItems: List<Item>) {
        reorderedItems.forEachIndexed { index, item ->
            repository.updateSortOrder(item.id, index)
        }
    }

    fun uncheckItem(itemId: String) {
        viewModelScope.launch { repository.removeConfirmation(itemId, today) }
    }

    fun confirmItem(itemId: String, photoPath: String? = null) {
        viewModelScope.launch {
            repository.insertConfirmation(
                Confirmation(
                    itemId = itemId,
                    date = today,
                    confirmedAt = System.currentTimeMillis(),
                    photoPath = photoPath
                )
            )
            _expandedItemId.value = null
        }
    }

    private suspend fun seedDefaultsIfNeeded() {
        val existing = repository.getAllItems().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                "Lock the door",
                "Stove off",
                "Took medication",
                "Close windows",
                "Feed the pet"
            )
            defaults.forEachIndexed { index, label ->
                repository.insertItem(Item(label = label, sortOrder = index))
            }
        }
    }
}
