package com.yep.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "confirmations")
data class Confirmation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val date: String,       // "2026-04-02"
    val confirmedAt: Long,
    val photoPath: String?  // local file path or null
)
