package com.charles.app.dreamloom.llm

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.Role
import com.google.ai.edge.litertlm.SamplerConfig
import com.charles.app.dreamloom.llm.backend.GpuDetector
import com.charles.app.dreamloom.llm.backend.QnnDetector
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@Singleton
class LlmEngine @Inject constructor() {

    private val generationDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "dreamloom-llm")
    }.asCoroutineDispatcher()

    private val _state = MutableStateFlow<LlmEngineState>(LlmEngineState.NotLoaded)
    val state: StateFlow<LlmEngineState> = _state.asStateFlow()

    @Volatile
    private var engine: Engine? = null

    private fun preferredBackend(): Backend = when {
        // Qualcomm Hexagon NPU when present (Snapdragon 8-class).
        QnnDetector.hasHexagon() -> Backend.NPU()
        // GPU only when OpenCL is actually loadable AND we're on a vendor where LiteRT-LM's
        // OpenCL path is known to work. On Google Tensor (Pixels) it crashes at inference
        // with "Can not find OpenCL library on this device" even when libOpenCL.so exists,
        // so we don't risk it there — CPU on a Cortex-X3/A715 cluster handles Gemma 4 E2B int4.
        GpuDetector.hasOpenCl() && isQualcomm() -> Backend.GPU()
        else -> Backend.CPU()
    }

    private fun isQualcomm(): Boolean =
        when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ->
                (android.os.Build.SOC_MANUFACTURER ?: "").contains("Qualcomm", ignoreCase = true)
            else -> (android.os.Build.MANUFACTURER ?: "").contains("Qualcomm", ignoreCase = true)
        }

    /**
     * Loads the on-disk model. Thread-safe; safe to call multiple times.
     *
     * Memory note: the model itself is ~2.58 GB on disk; LiteRT-LM also allocates a
     * KV cache proportional to [maxNumTokens] and per-image vision encoder state.
     * On a Pixel 8 Pro the OS LMK starts killing the process around ~3 GB resident,
     * so keep these tight: the interpretation prompt is ~600 tokens, output ~200,
     * and we only ever attach one photo.
     */
    suspend fun ensureLoaded(modelFile: File) = withContext(generationDispatcher) {
        synchronized(this@LlmEngine) {
            if (engine != null) return@withContext
            _state.value = LlmEngineState.Loading(0.05f)
        }
        require(modelFile.exists()) { "Model file missing at ${modelFile.absolutePath}" }
        val backendsToTry = buildList {
            val preferred = preferredBackend()
            add(preferred)
            // Always have CPU as last-resort fallback (GPU/NPU init can fail or OOM).
            if (preferred !is Backend.CPU) add(Backend.CPU())
        }
        var lastError: Throwable? = null
        for (backend in backendsToTry) {
            try {
                val options = EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = backend,
                    visionBackend = backend,
                    audioBackend = Backend.CPU(),
                    maxNumTokens = 4_096,
                    maxNumImages = 1,
                    cacheDir = null,
                )
                val e = Engine(options)
                e.initialize()
                synchronized(this@LlmEngine) {
                    engine = e
                }
                _state.value = LlmEngineState.Ready
                return@withContext
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                lastError = t
                // Keep going to the next backend (likely CPU).
            }
        }
        synchronized(this@LlmEngine) { engine = null }
        _state.value = LlmEngineState.Error(lastError ?: IllegalStateException("LlmEngine init failed"))
        throw lastError ?: IllegalStateException("LlmEngine init failed")
    }

    /**
     * Streams **delta** text chunks (model output only) for a single user turn.
     *
     * [systemPrompt] goes into the engine's system slot — do NOT pre-wrap it in chat template
     * tokens; LiteRT-LM applies the model's correct template at tokenization time. Same for
     * [userMessage]: pass plain text. Hand-rolled `<start_of_turn>...` wrappers are wrong
     * for Gemma 4 (uses `<|turn>...<turn|>`) and double-templating breaks output quality.
     */
    fun generateStream(
        userMessage: String,
        systemPrompt: String = "",
        imageFile: File? = null,
        topK: Int = GenParams.INTERPRET_TOP_K,
        topP: Double = GenParams.INTERPRET_TOP_P.toDouble(),
        temperature: Double = GenParams.INTERPRET_TEMPERATURE.toDouble(),
        maxTokens: Int = GenParams.INTERPRET_MAX_TOKENS,
    ): Flow<String> = channelFlow {
        val eng = engine ?: error("LlmEngine not loaded")
        // Do not pin a fixed seed: it makes generations feel repetitive across dreams.
        val sampler = SamplerConfig(topK, topP, temperature, Random.nextInt())
        val config = ConversationConfig(
            Contents.of(systemPrompt),
            emptyList(),
            emptyList(),
            sampler,
        )
        val conv = eng.createConversation(config)
        try {
            val initial = if (imageFile != null) {
                Message.user(
                    Contents.of(
                        listOf(
                            Content.Text(userMessage),
                            Content.ImageFile(imageFile.absolutePath),
                        ),
                    ),
                )
            } else {
                Message.user(userMessage)
            }
            var lastModelText = ""
            conv.sendMessageAsync(initial, emptyMap()).collect { msg ->
                if (msg.role == Role.MODEL) {
                    val text = textFromMessage(msg)
                    if (text.length > lastModelText.length) {
                        send(text.substring(lastModelText.length))
                        lastModelText = text
                    }
                }
            }
        } finally {
            runCatching { conv.close() }
        }
    }.flowOn(generationDispatcher)

    fun release() {
        runCatching {
            engine?.close()
        }
        engine = null
        _state.value = LlmEngineState.NotLoaded
    }

    private fun textFromMessage(msg: Message): String = buildString {
        val body = msg.contents
        for (c in body.contents) {
            when (c) {
                is Content.Text -> append(c.text)
                else -> { }
            }
        }
    }
}

