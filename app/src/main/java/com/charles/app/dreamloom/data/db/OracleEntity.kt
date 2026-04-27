package com.charles.app.dreamloom.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oracles")
data class OracleEntity(
    @PrimaryKey val id: Long,
    val createdAt: Long,
    val question: String,
    val answer: String,
)
