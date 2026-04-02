package com.yep.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.entities.DailyItemSnapshot
import com.yep.app.data.entities.Item
import com.yep.app.data.entities.StreakData
import com.yep.app.data.entities.UserSettings

@Database(
    entities = [Item::class, Confirmation::class, UserSettings::class, StreakData::class, DailyItemSnapshot::class],
    version = 3,
    exportSchema = false
)
abstract class YepDatabase : RoomDatabase() {
    abstract fun yepDao(): YepDao
}
