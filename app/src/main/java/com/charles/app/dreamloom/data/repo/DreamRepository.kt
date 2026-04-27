package com.charles.app.dreamloom.data.repo

import com.charles.app.dreamloom.data.db.DreamDao
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.llm.Interpretation
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DreamRepository @Inject constructor(
    private val dao: DreamDao,
) {
    fun observeAll(): Flow<List<DreamEntity>> = dao.observeAll()
    fun byId(id: Long): Flow<DreamEntity?> = dao.observeById(id)
    suspend fun getById(id: Long): DreamEntity? = dao.getById(id)
    fun searchFts(q: String): Flow<List<DreamEntity>> = dao.searchFts(q)

    suspend fun create(
        id: Long,
        rawText: String,
        mood: String,
        photoPath: String?,
        audioPath: String?,
    ) {
        dao.insert(
            DreamEntity(
                id = id,
                createdAt = id,
                rawText = rawText,
                mood = mood,
                photoPath = photoPath,
                title = null,
                symbolsJson = null,
                interpretation = null,
                intention = null,
                modelVersion = null,
                isInterpretationComplete = false,
                audioPath = audioPath,
            ),
        )
    }

    suspend fun attachInterpretation(
        id: Long,
        interp: Interpretation,
        modelVersion: String,
    ) {
        val cur = dao.getById(id) ?: return
        val syms = JSONArray().apply { interp.symbols.forEach { put(it) } }.toString()
        dao.update(
            cur.copy(
                title = interp.title,
                symbolsJson = syms,
                interpretation = interp.interpretation,
                intention = interp.intention,
                modelVersion = modelVersion,
                isInterpretationComplete = true,
            ),
        )
    }

    suspend fun attachPartialInterpretation(id: Long, rawText: String) {
        val cur = dao.getById(id) ?: return
        dao.update(
            cur.copy(
                interpretation = rawText,
                isInterpretationComplete = false,
            ),
        )
    }

    suspend fun updateExtended(id: Long, text: String) {
        val cur = dao.getById(id) ?: return
        dao.update(cur.copy(extendedInterpretation = text))
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun wipeAll() = dao.deleteAll()
    suspend fun countAll(): Int = dao.countAll()
}
