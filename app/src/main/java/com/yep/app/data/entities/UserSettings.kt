package com.yep.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class UserSettings(
    @PrimaryKey val id: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:30",
    val onboardingComplete: Boolean = false,
    val theme: String = "system"
)
