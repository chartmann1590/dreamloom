package com.charles.app.dreamloom.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InsightEntity)

    @Query("SELECT * FROM insights ORDER BY weekStartEpochDay DESC")
    fun observeAll(): Flow<List<InsightEntity>>

    @Query("DELETE FROM insights")
    suspend fun deleteAll()
}
