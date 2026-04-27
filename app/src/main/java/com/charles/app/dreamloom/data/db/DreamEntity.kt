package com.charles.app.dreamloom.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey val id: Long,
    val createdAt: Long,
    val rawText: String,
    val mood: String,
    val photoPath: String?,
    val title: String?,
    val symbolsJson: String?,
    val interpretation: String?,
    val intention: String?,
    val modelVersion: String?,
    val isInterpretationComplete: Boolean,
    val audioPath: String? = null,
    val extendedInterpretation: String? = null,
)
