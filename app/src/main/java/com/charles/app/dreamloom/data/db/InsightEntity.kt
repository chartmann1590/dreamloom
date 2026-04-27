package com.charles.app.dreamloom.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey val weekStartEpochDay: Long,
    val createdAt: Long,
    val pattern: String,
    val summary: String,
    val invitation: String,
    val topSymbolsJson: String,
)
