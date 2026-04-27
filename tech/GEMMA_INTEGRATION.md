# Gemma 4 E2B via LiteRT-LM — Integration Recipe

This document explains how the app runs the on-device LLM.

We use **Gemma 4 E2B IT** (instruction-tuned), int4 quantized, via the **LiteRT-LM Android API**. Source: `huggingface.co/litert-community/gemma-4-E2B-it-litert-lm`.

---

## Model file

| Attribute | Value |
| --- | --- |
| File name | `gemma-4-E2B-it-litert-lm.task` (or `.litertlm`) |
| Size | ~2.58 GB |
| Quantization | int4 (weights) + int8 activations |
| Memory footprint at runtime | ~1.7 GB on CPU, ~1.5 GB on GPU |
| Context length | 32,768 tokens |
| License | Apache 2.0 |
| SHA-256 | (record at build time, embed as a constant for verification) |

We do NOT bundle this in the APK. We host it on **GitHub Releases** under the project repo and download on first launch via WorkManager. See `tech/MODEL_DOWNLOAD.md`.

---

## Gradle dependency

In `app/build.gradle.kts`:

```kotlin
implementation("com.google.ai.edge.litert:litert-genai:1.0.0-beta01")
implementation("com.google.ai.edge.litert:litert:1.0.1")
```

If a newer non-beta is published, prefer it. Verify the API surface — the constructor names below may have stabilized.

---

## The `LlmEngine` singleton

The whole engine lives in one class. It is `@Singleton` (Hilt) and held by the application process.

```kotlin
package app.dreamloom.llm

import android.content.Context
import com.google.ai.edge.litert.genai.GenAI
import com.google.ai.edge.litert.genai.GenAIModelOptions
import com.google.ai.edge.litert.genai.Backend
import com.google.ai.edge.litert.genai.GenerationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmEngine @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {

    private val generationDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val _state = MutableStateFlow<LlmEngineState>(LlmEngineState.NotLoaded)
    val state: StateFlow<LlmEngineState> = _state

    private var engine: GenAI? = null

    suspend fun ensureLoaded() = withContext(generationDispatcher) {
        if (engine != null) return@withContext
        _state.value = LlmEngineState.Loading(0f)
        try {
            val modelFile = ModelStorage.modelFile(ctx)
            require(modelFile.exists()) { "Model file missing at ${modelFile.absolutePath}" }

            val options = GenAIModelOptions().apply {
                setModelPath(modelFile.absolutePath)
                setBackend(preferredBackend())  // GPU on supported devices, else CPU
                setMaxNumTokens(32_768)
            }
            engine = GenAI.createModel(options)
            _state.value = LlmEngineState.Ready
        } catch (t: Throwable) {
            engine = null
            _state.value = LlmEngineState.Error(t)
            throw t
        }
    }

    /**
     * Streams tokens from Gemma. Caller is responsible for assembling them.
     * Multimodal: if [imageFile] is non-null, the model receives it as the
     * first turn-input alongside the text prompt.
     */
    fun generateStream(
        prompt: String,
        config: GenerationConfig,
        imageFile: File? = null,
    ): Flow<String> = callbackFlow {
        val activeEngine = engine
            ?: error("LlmEngine.generateStream called before ensureLoaded()")

        val session = activeEngine.createSession(config)
        try {
            if (imageFile != null) {
                session.addImage(imageFile)  // Gemma 4 multimodal slot
            }
            session.addQuery(prompt)
            session.streamResponse { chunk, isDone ->
                trySend(chunk)
                if (isDone) close()
            }
            awaitClose { runCatching { session.close() } }
        } catch (t: Throwable) {
            close(t)
            runCatching { session.close() }
        }
    }.flowOn(generationDispatcher)

    /** Convenience: collect the stream into a single String. */
    suspend fun generate(
        prompt: String,
        config: GenerationConfig,
        imageFile: File? = null,
    ): String = buildString {
        generateStream(prompt, config, imageFile).collect { append(it) }
    }

    fun release() {
        runCatching { engine?.close() }
        engine = null
        _state.value = LlmEngineState.NotLoaded
    }

    private fun preferredBackend(): Backend {
        // Detect QNN / NPU support; fall back gracefully.
        return when {
            QnnDetector.hasHexagon(ctx) -> Backend.NPU
            GpuDetector.hasOpenCl(ctx)  -> Backend.GPU
            else                        -> Backend.CPU
        }
    }
}

sealed class LlmEngineState {
    data object NotLoaded : LlmEngineState()
    data class  Loading(val progress: Float) : LlmEngineState()
    data object Ready : LlmEngineState()
    data class  Error(val cause: Throwable) : LlmEngineState()
}
```

> **Note:** the LiteRT-LM Android API surface is still beta as of v1.0.0-beta01. The exact method names (`createSession`, `addQuery`, `streamResponse`, `addImage`) may differ slightly in the released SDK. After adding the dependency, use Android Studio to inspect `GenAI` and adjust this file if needed. The overall flow—*create options → create model → create session → add input → stream → close*—stays the same.

---

## Backend detection helpers

```kotlin
object QnnDetector {
    fun hasHexagon(ctx: Context): Boolean {
        // Qualcomm SoCs since SD 8 Gen 1 expose Hexagon NPU via QNN.
        val socManufacturer = android.os.Build.SOC_MANUFACTURER ?: ""
        val socModel = android.os.Build.SOC_MODEL ?: ""
        return socManufacturer.contains("Qualcomm", ignoreCase = true) &&
               socModel.matches(Regex("SM\\d{4}")) // crude but workable
    }
}

object GpuDetector {
    fun hasOpenCl(ctx: Context): Boolean {
        // OpenCL is present on essentially all Adreno + Mali GPUs we target.
        return true
    }
}
```

For first launch we are conservative: **default to GPU**. NPU adds complexity (per-vendor SDKs); we add it as a v1.1 optimization and ship CPU/GPU only on day one.

---

## Generation config defaults

Keep these as constants in `llm/GenParams.kt` so prompt callers stay clean:

```kotlin
object GenParams {
    val INTERPRET = GenerationConfig.Builder()
        .setTemperature(0.85f)
        .setTopP(0.95f)
        .setTopK(64)
        .setMaxTokens(320)
        .build()

    val REINTERPRET = GenerationConfig.Builder()
        .setTemperature(1.0f).setTopP(0.95f).setTopK(64).setMaxTokens(320).build()

    val EXTENDED = GenerationConfig.Builder()
        .setTemperature(0.85f).setTopP(0.95f).setTopK(64).setMaxTokens(600).build()

    val INSIGHT = GenerationConfig.Builder()
        .setTemperature(0.7f).setTopP(0.9f).setTopK(40).setMaxTokens(300).build()

    val ORACLE = GenerationConfig.Builder()
        .setTemperature(0.85f).setTopP(0.95f).setTopK(64).setMaxTokens(220).build()
}
```

---

## Prompt builder — chat template

Gemma 4 uses the standard Gemma chat template:

```
<start_of_turn>user
{system instructions}

{user content}<end_of_turn>
<start_of_turn>model
```

LiteRT-LM's `addQuery` typically wraps the chat template automatically. If it doesn't (verify in your SDK version), use this helper:

```kotlin
fun gemmaChat(system: String, user: String) = buildString {
    append("<start_of_turn>user\n")
    append(system.trim())
    append("\n\n")
    append(user.trim())
    append("<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
```

The system prompt content lives in `spec/PROMPTS.md` — copy verbatim into Kotlin string constants in `llm/prompts/`.

---

## DreamInterpreter — the only consumer

```kotlin
class DreamInterpreter @Inject constructor(
    private val llm: LlmEngine,
) {
    suspend fun interpret(
        dreamText: String,
        mood: String,
        photoFile: File? = null,
    ): Flow<InterpretChunk> = flow {
        llm.ensureLoaded()
        val user = if (photoFile != null) {
            InterpretPrompts.userWithImage(dreamText, mood)
        } else {
            InterpretPrompts.user(dreamText, mood)
        }
        val full = StringBuilder()
        llm.generateStream(
            prompt = gemmaChat(InterpretPrompts.SYSTEM, user),
            config = GenParams.INTERPRET,
            imageFile = photoFile,
        ).collect { chunk ->
            full.append(chunk)
            emit(InterpretChunk.Partial(full.toString()))
        }
        val parsed = parseInterpretation(full.toString())
        if (parsed == null) emit(InterpretChunk.Failed(full.toString()))
        else emit(InterpretChunk.Done(parsed))
    }
}

sealed class InterpretChunk {
    data class Partial(val raw: String) : InterpretChunk()
    data class Done(val interp: Interpretation) : InterpretChunk()
    data class Failed(val raw: String) : InterpretChunk()
}
```

`parseInterpretation` lives in `spec/PROMPTS.md` — copy that function into `llm/Parsers.kt`.

---

## Memory & lifecycle rules

- **Never** call `LlmEngine.release()` on configuration changes.
- **Never** call it from a Composable or VM `onCleared()`.
- **Only** call it from:
  - `DreamloomApplication.onTrimMemory(LEVEL_COMPLETE)`
  - Settings → "Reload model" (debug only)
  - The wipe-everything flow (after the user confirms)

If we release prematurely, the next dream entry forces a 5-second cold load. We protect that path carefully.

---

## Performance targets

| Device class | Backend | First token | Sustained tok/s |
| --- | --- | --- | --- |
| Pixel 9 / S25 (flagship) | GPU | < 500ms | 30–50 |
| Pixel 7a / mid-range | CPU | < 1.2s | 8–14 |
| Budget (4 GB RAM) | CPU | < 2.5s | 4–6 |

If sustained tok/s on the active backend drops below 4, fall back to a "lite" mode: shorter `max_tokens` and a tighter system prompt. Don't expose this to users; just degrade gracefully.

---

## Verification on first build

A debug-only screen at `Settings → About → Run model self-test` (only in `BuildConfig.DEBUG`) that:

1. Loads the engine.
2. Generates a fixed prompt: `"Say hello in five words."`
3. Asserts response is non-empty.
4. Reports first-token latency, total time, tok/s.

Use this to confirm a correct build. If it fails, the model file is corrupt or the SDK API drifted — both are caught early.
