package com.charles.app.dreamloom.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OracleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OracleEntity)

    @Query("SELECT * FROM oracles ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OracleEntity>>

    @Query("DELETE FROM oracles")
    suspend fun deleteAll()
}
