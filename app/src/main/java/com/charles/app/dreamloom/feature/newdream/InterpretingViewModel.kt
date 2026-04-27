package com.charles.app.dreamloom.feature.newdream

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.data.repo.DreamRepository
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InterpretingViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val dreams: DreamRepository,
    private val interpreter: DreamInterpreter,
    private val engine: LlmEngine,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: Long = savedStateHandle.get<Long>("id")!!

    private val _raw = MutableStateFlow("")
    val raw: StateFlow<String> = _raw.asStateFlow()
    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

    init {
        viewModelScope.launch {
            val entity = dreams.getById(id) ?: return@launch
            if (!BuildConfig.USE_FAKE_LLM) {
                runCatching { engine.ensureLoaded(ModelStorage.modelFile(app)) }
            }
            val photo = entity.photoPath?.let { File(it) }?.takeIf { it.exists() }
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
                        _done.value = true
                    }
                    is InterpretChunk.Failed -> {
                        _raw.value = chunk.raw
                    }
                }
            }
        }
    }
}
