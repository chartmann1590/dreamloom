package com.charles.app.dreamloom.feature.oracle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.db.OracleDao
import com.charles.app.dreamloom.data.db.OracleEntity
import com.charles.app.dreamloom.data.model.startOfLast30DaysMs
import com.charles.app.dreamloom.data.model.symbolIndexFromDreams
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import com.charles.app.dreamloom.llm.GenParams
import com.charles.app.dreamloom.llm.LlmEngine
import com.charles.app.dreamloom.llm.ModelStorage
import com.charles.app.dreamloom.llm.OracleResponder
import com.charles.app.dreamloom.llm.prompts.InsightOraclePrompts
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OracleViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val engine: LlmEngine,
    private val dreams: DreamRepository,
    private val oracleDao: OracleDao,
    private val analytics: DreamloomAnalytics,
) : ViewModel() {
    val oracles: StateFlow<List<OracleEntity>> = oracleDao
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun ask(question: String) = viewModelScope.launch {
        val q = question.trim()
        if (q.isEmpty()) return@launch
        _error.value = null
        _busy.value = true
        try {
            val id = System.currentTimeMillis()
            val since = startOfLast30DaysMs()
            val top = withContext(Dispatchers.IO) {
                val recent = dreams.interpretedDreamsSince(since)
                symbolIndexFromDreams(recent).take(10)
            }
            val model = ModelStorage.modelFile(app)
            if (!model.exists()) {
                _error.value = "Model is not on this device yet."
                return@launch
            }
            val answerBuf = StringBuilder()
            withContext(Dispatchers.IO) {
                runCatching { engine.ensureLoaded(model) }
                    .onFailure { throw it }
                val responder = OracleResponder(engine)
                answerBuf.append(responder.answer(q, top))
            }
            val answer = answerBuf.toString().trim()
            if (answer.isEmpty()) {
                _error.value = "No answer was woven. Try again."
                return@launch
            }
            withContext(Dispatchers.IO) {
                oracleDao.insert(OracleEntity(id, id, q, answer))
            }
            analytics.logOracleAnswered()
        } catch (e: Exception) {
            _error.value = e.message ?: "Something went wrong."
        } finally {
            _busy.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

}
