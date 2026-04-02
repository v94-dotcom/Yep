package com.yep.app.data.entities

import androidx.room.Entity

@Entity(
    tableName = "daily_item_snapshots",
    primaryKeys = ["date", "itemId"]
)
data class DailyItemSnapshot(
    val date: String,
    val itemId: String,
    val itemLabel: String
)
