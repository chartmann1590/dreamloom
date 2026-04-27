package com.charles.app.dreamloom.llm

import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.llm.prompts.InterpretPrompts
import com.charles.app.dreamloom.llm.prompts.gemmaChat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class InterpretChunk {
    data class Partial(val raw: String) : InterpretChunk()
    data class Done(val interp: Interpretation) : InterpretChunk()
    data class Failed(val raw: String) : InterpretChunk()
}

@Singleton
class DreamInterpreter @Inject constructor(
    private val llm: LlmEngine,
) {
    suspend fun interpret(
        dreamText: String,
        mood: String,
        photoFile: File? = null,
    ): Flow<InterpretChunk> = flow {
        if (BuildConfig.USE_FAKE_LLM) {
            val text = FAKE_INTERP
            var acc = ""
            for (part in text.chunked(8)) {
                acc += part
                emit(InterpretChunk.Partial(acc))
                delay(40)
            }
            val p = parseInterpretation(acc)
            if (p != null) emit(InterpretChunk.Done(p)) else emit(InterpretChunk.Failed(acc))
            return@flow
        }
        val user = if (photoFile != null) {
            InterpretPrompts.userWithImage(dreamText, mood)
        } else {
            InterpretPrompts.user(dreamText, mood)
        }
        val fullPrompt = gemmaChat(InterpretPrompts.SYSTEM, user)
        val acc = StringBuilder()
        llm.generateStream(
            userMessage = fullPrompt,
            imageFile = photoFile,
        ).collect { chunk ->
            acc.append(chunk)
            emit(InterpretChunk.Partial(acc.toString()))
        }
        val parsed = parseInterpretation(acc.toString())
        if (parsed == null) emit(InterpretChunk.Failed(acc.toString())) else emit(InterpretChunk.Done(parsed))
    }

    private companion object {
        val FAKE_INTERP = """
            TITLE: Falling through warm water
            SYMBOLS: water, falling, light
            INTERPRETATION: You moved downward, but slowly, as if held by something that did not want to drop you. The water was warm — that is the part to listen to. Falling, in dreams, often names a release; warm water names a being-held. Together they suggest you are letting go of something you have been carrying alone, and noticing for the first time that the letting go has become safe.
            INTENTION: Today, let one thing happen without your hand on it.
        """.trimIndent()
    }
}
