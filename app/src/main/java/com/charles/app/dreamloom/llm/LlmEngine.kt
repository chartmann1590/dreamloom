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
        QnnDetector.hasHexagon() -> Backend.NPU()
        GpuDetector.hasOpenCl() -> Backend.GPU()
        else -> Backend.CPU()
    }

    /**
     * Loads the on-disk model. Thread-safe; safe to call multiple times.
     */
    suspend fun ensureLoaded(modelFile: File) = withContext(generationDispatcher) {
        synchronized(this@LlmEngine) {
            if (engine != null) return@withContext
            _state.value = LlmEngineState.Loading(0.05f)
        }
        try {
            require(modelFile.exists()) { "Model file missing at ${modelFile.absolutePath}" }
            val options = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = preferredBackend(),
                visionBackend = preferredBackend(),
                audioBackend = Backend.CPU(),
                maxNumTokens = 32_768,
                maxNumImages = 4,
                cacheDir = null,
            )
            val e = Engine(options)
            e.initialize()
            synchronized(this@LlmEngine) {
                engine = e
            }
            _state.value = LlmEngineState.Ready
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            synchronized(this@LlmEngine) { engine = null }
            _state.value = LlmEngineState.Error(t)
            throw t
        }
    }

    /**
     * Streams **delta** text chunks (model output only) for a single user turn.
     * [userMessage] is the full first user message (e.g. from [gemmaChat]).
     */
    fun generateStream(
        userMessage: String,
        imageFile: File? = null,
        topK: Int = GenParams.INTERPRET_TOP_K,
        topP: Double = GenParams.INTERPRET_TOP_P.toDouble(),
        temperature: Double = GenParams.INTERPRET_TEMPERATURE.toDouble(),
        maxTokens: Int = GenParams.INTERPRET_MAX_TOKENS,
    ): Flow<String> = channelFlow {
        val eng = engine ?: error("LlmEngine not loaded")
        val sampler = SamplerConfig(topK, topP, temperature, 0)
        val config = ConversationConfig(
            Contents.of(""),
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

