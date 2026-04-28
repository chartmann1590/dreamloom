package com.charles.app.dreamloom.feature.newdream

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import com.charles.app.dreamloom.llm.DreamInterpreter
import com.charles.app.dreamloom.llm.InterpretChunk
import com.charles.app.dreamloom.llm.LlmEngine
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.llm.ModelStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InterpretingViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val dreams: DreamRepository,
    private val interpreter: DreamInterpreter,
    private val engine: LlmEngine,
    private val analytics: DreamloomAnalytics,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: Long = savedStateHandle.get<Long>("id")
        ?: error("dream id required")

    private val _raw = MutableStateFlow("")
    val raw: StateFlow<String> = _raw.asStateFlow()

    /** When true, streaming ended (success, failure, or cancelled processing). */
    private val _streamFinished = MutableStateFlow(false)
    val streamFinished: StateFlow<Boolean> = _streamFinished.asStateFlow()

    private val _streamSuccess = MutableStateFlow(false)
    val streamSuccess: StateFlow<Boolean> = _streamSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            val entity = dreams.getById(id) ?: return@launch
            runCatching { engine.ensureLoaded(ModelStorage.modelFile(app)) }
            val photo = entity.photoPath?.let { File(it) }?.takeIf { it.exists() }
            val hadPhoto = photo != null
            interpreter.interpret(entity.rawText, entity.mood, photo).collect { chunk ->
                when (chunk) {
                    is InterpretChunk.Partial -> {
                        _raw.value = chunk.raw
                    }
                    is InterpretChunk.Done -> {
                        dreams.attachInterpretation(
                            id,
                            chunk.interp,
                            "gemma-4-e2b-${ModelConfig.VERSION}",
                        )
                        analytics.logInterpretationCompleted(hadPhoto)
                        _streamSuccess.value = true
                        _streamFinished.value = true
                    }
                    is InterpretChunk.Failed -> {
                        _raw.value = chunk.raw
                        if (chunk.raw.isNotBlank()) {
                            dreams.attachPartialInterpretation(id, chunk.raw)
                        }
                        analytics.logInterpretationFailed(hadPhoto)
                        _streamSuccess.value = false
                        _streamFinished.value = true
                    }
                }
            }
        }
    }

    override fun onCleared() {
        if (!_streamFinished.value && _raw.value.isNotBlank()) {
            runBlocking {
                runCatching { dreams.attachPartialInterpretation(id, _raw.value) }
            }
        }
        super.onCleared()
    }
}
