package com.yep.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yep.app.data.entities.*

@Database(
    entities = [Item::class, Confirmation::class, UserSettings::class, StreakData::class],
    version = 1,
    exportSchema = false
)
abstract class YepDatabase : RoomDatabase() {
    abstract fun yepDao(): YepDao
}
