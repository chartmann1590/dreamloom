package com.charles.app.dreamloom.data.db

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = DreamEntity::class)
@Entity(tableName = "dreams_fts")
data class DreamFts(
    val rawText: String,
)
