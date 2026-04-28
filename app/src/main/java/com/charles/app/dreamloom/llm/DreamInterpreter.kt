package com.charles.app.dreamloom.llm

import com.charles.app.dreamloom.llm.prompts.InterpretPrompts
import com.charles.app.dreamloom.llm.prompts.gemmaChat
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
}
