package com.charles.app.dreamloom.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DreamEntity)

    @Update
    suspend fun update(entity: DreamEntity)

    @Query("SELECT * FROM dreams WHERE id = :id")
    fun observeById(id: Long): Flow<DreamEntity?>

    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun getById(id: Long): DreamEntity?

    @Query("SELECT * FROM dreams ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DreamEntity>>

    @Query("SELECT d.* FROM dreams d INNER JOIN dreams_fts ON d.rowid = dreams_fts.rowid WHERE dreams_fts MATCH :query ORDER BY d.createdAt DESC")
    fun searchFts(query: String): Flow<List<DreamEntity>>

    @Query("SELECT COUNT(*) FROM dreams")
    suspend fun countAll(): Int

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM dreams")
    suspend fun deleteAll()
}
