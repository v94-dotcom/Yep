package com.yep.app.data.repository

import com.yep.app.data.YepDao
import com.yep.app.data.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YepRepository @Inject constructor(private val dao: YepDao) {

    fun getAllItems(): Flow<List<Item>> = dao.getAllItems()

    suspend fun insertItem(item: Item) = dao.insertItem(item)

    suspend fun deleteItem(item: Item) = dao.deleteItem(item)

    suspend fun updateSortOrder(id: String, order: Int) = dao.updateSortOrder(id, order)

    fun getConfirmationsForDate(date: String): Flow<List<Confirmation>> =
        dao.getConfirmationsForDate(date)

    suspend fun insertConfirmation(confirmation: Confirmation) =
        dao.insertConfirmation(confirmation)

    suspend fun removeConfirmation(itemId: String, date: String) =
        dao.removeConfirmation(itemId, date)

    suspend fun getConfirmationsBeforeDate(date: String): List<Confirmation> =
        dao.getConfirmationsBeforeDate(date)

    suspend fun deleteConfirmationsBeforeDate(date: String) =
        dao.deleteConfirmationsBeforeDate(date)

    fun getConfirmationsSince(startDate: String): Flow<List<Confirmation>> =
        dao.getConfirmationsSince(startDate)

    fun getSettings(): Flow<UserSettings?> = dao.getSettings()

    suspend fun saveSettings(settings: UserSettings) = dao.saveSettings(settings)

    fun getStreak(): Flow<StreakData?> = dao.getStreak()

    suspend fun saveStreak(streak: StreakData) = dao.saveStreak(streak)
}
