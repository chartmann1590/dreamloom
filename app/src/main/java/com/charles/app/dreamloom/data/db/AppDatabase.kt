package com.charles.app.dreamloom.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DreamEntity::class,
        DreamFts::class,
        InsightEntity::class,
        OracleEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun insightDao(): InsightDao
    abstract fun oracleDao(): OracleDao
}
