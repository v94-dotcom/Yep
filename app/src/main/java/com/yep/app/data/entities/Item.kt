package com.yep.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val label: String,
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)
