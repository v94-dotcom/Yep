package com.yep.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak")
data class StreakData(
    @PrimaryKey val id: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompleteDate: String? = null
)
