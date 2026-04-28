package com.charles.app.dreamloom.feature.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.ads.AdGate
import com.charles.app.dreamloom.ads.InterstitialManager
import com.charles.app.dreamloom.data.prefs.AppPreferences
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DreamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val app: Context,
    private val repo: DreamRepository,
    private val interpreter: DreamInterpreter,
    private val engine: LlmEngine,
    private val adGate: AdGate,
    private val appPreferences: AppPreferences,
    private val interstitialManager: InterstitialManager,
    private val analytics: DreamloomAnalytics,
) : ViewModel() {
    private val id: Long = savedStateHandle.get<Long>("id") ?: 0L

    val dream = repo.byId(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _reInterpreting = MutableStateFlow(false)
    val reInterpreting: StateFlow<Boolean> = _reInterpreting.asStateFlow()

    fun preloadInterstitial() {
        interstitialManager.preload()
    }

    suspend fun canShowInterstitialOnBack(): Boolean {
        if (id == 0L) return false
        val count = repo.countAll()
        val lastAt = appPreferences.lastInterstitialAt.first()
        val thisSession = appPreferences.interstitialThisSession.first()
        val sessionStart = appPreferences.sessionStartMs.first()
        if (sessionStart == 0L) return false
        return adGate.canShowInterstitial(
            dreamCountLifetime = count,
            sessionStartMs = sessionStart,
            alreadyThisSession = thisSession,
            lastInterstitialAtMs = lastAt,
        )
    }

    fun recordInterstitialShown() = viewModelScope.launch {
        appPreferences.setInterstitialShown()
    }

    fun reInterpret() = viewModelScope.launch {
        _reInterpreting.value = true
        try {
            val ent = repo.getById(id) ?: return@launch
            runCatching { engine.ensureLoaded(ModelStorage.modelFile(app)) }
            val photo = ent.photoPath?.let { File(it) }?.takeIf { it.exists() }
            val hadPhoto = photo != null
            interpreter.interpret(ent.rawText, ent.mood, photo).collect { chunk ->
                when (chunk) {
                    is InterpretChunk.Partial -> {}
                    is InterpretChunk.Done -> {
                        repo.attachInterpretation(
                            id,
                            chunk.interp,
                            "gemma-4-e2b-${ModelConfig.VERSION}",
                        )
                        analytics.logReinterpretCompleted(hadPhoto)
                    }
                    is InterpretChunk.Failed -> {
                        repo.attachPartialInterpretation(id, chunk.raw)
                        analytics.logReinterpretFailed(hadPhoto)
                    }
                }
            }
        } finally {
            _reInterpreting.value = false
        }
    }

    fun showInterstitialIfPermitted(
        activity: android.app.Activity,
        onAfter: (adShown: Boolean) -> Unit,
    ) {
        if (id == 0L) {
            onAfter(false)
            return
        }
        interstitialManager.showIfReady(activity) { shown ->
            onAfter(shown)
        }
    }

    fun deleteDream(onComplete: () -> Unit) = viewModelScope.launch {
        repo.delete(id)
        analytics.logDreamDeleted()
        onComplete()
    }
}
