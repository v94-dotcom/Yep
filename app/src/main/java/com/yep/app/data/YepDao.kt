package com.yep.app.data

import androidx.room.*
import com.yep.app.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface YepDao {

    // Items
    @Query("SELECT * FROM items ORDER BY sortOrder ASC")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("UPDATE items SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)

    // Confirmations
    @Query("SELECT * FROM confirmations WHERE date = :date")
    fun getConfirmationsForDate(date: String): Flow<List<Confirmation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmation(confirmation: Confirmation)

    @Query("DELETE FROM confirmations WHERE itemId = :itemId AND date = :date")
    suspend fun removeConfirmation(itemId: String, date: String)

    @Query("SELECT * FROM confirmations WHERE date < :date")
    suspend fun getConfirmationsBeforeDate(date: String): List<Confirmation>

    @Query("DELETE FROM confirmations WHERE date < :date")
    suspend fun deleteConfirmationsBeforeDate(date: String)

    @Query("SELECT * FROM confirmations WHERE date >= :startDate")
    fun getConfirmationsSince(startDate: String): Flow<List<Confirmation>>

    // Daily Item Snapshots
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyItemSnapshot(snapshot: DailyItemSnapshot)

    @Query("SELECT * FROM daily_item_snapshots WHERE date >= :startDate")
    fun getSnapshotsSince(startDate: String): Flow<List<DailyItemSnapshot>>

    @Query("DELETE FROM daily_item_snapshots WHERE date < :date")
    suspend fun deleteSnapshotsBeforeDate(date: String)

    // Settings
    @Query("SELECT * FROM settings WHERE id = 0")
    fun getSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)

    // Streak
    @Query("SELECT * FROM streak WHERE id = 0")
    fun getStreak(): Flow<StreakData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStreak(streak: StreakData)
}
