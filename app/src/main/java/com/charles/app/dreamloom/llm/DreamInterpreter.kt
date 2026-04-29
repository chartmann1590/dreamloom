package com.charles.app.dreamloom.llm

import com.charles.app.dreamloom.llm.prompts.InterpretPrompts
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.Locale
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
        emitAllInterpretation(
            user = user,
            dreamText = dreamText,
            mood = mood,
            previousSymbols = emptyList(),
            photoFile = photoFile,
            topK = GenParams.INTERPRET_TOP_K,
            topP = GenParams.INTERPRET_TOP_P.toDouble(),
            temperature = GenParams.INTERPRET_TEMPERATURE.toDouble(),
            maxTokens = GenParams.INTERPRET_MAX_TOKENS,
        )
    }

    suspend fun reInterpret(
        dreamText: String,
        mood: String,
        previousSymbols: List<String>,
        photoFile: File? = null,
    ): Flow<InterpretChunk> = flow {
        val previous = previousSymbols.joinToString(", ").ifBlank { "none" }
        val userBase = InterpretPrompts.reInterpret(dreamText, previous)
        val user = if (photoFile != null) {
            "$userBase\n\nThe dreamer's mood was: $mood.\n\nA dream image is attached. Use it to find fresh symbols."
        } else {
            "$userBase\n\nThe dreamer's mood was: $mood."
        }
        emitAllInterpretation(
            user = user,
            dreamText = dreamText,
            mood = mood,
            previousSymbols = previousSymbols,
            photoFile = photoFile,
            topK = 64,
            topP = 0.95,
            temperature = 1.0,
            maxTokens = 360,
        )
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<InterpretChunk>.emitAllInterpretation(
        user: String,
        dreamText: String,
        mood: String,
        previousSymbols: List<String>,
        photoFile: File?,
        topK: Int,
        topP: Double,
        temperature: Double,
        maxTokens: Int,
    ) {
        val acc = StringBuilder()
        llm.generateStream(
            userMessage = user,
            systemPrompt = InterpretPrompts.SYSTEM,
            imageFile = photoFile,
            topK = topK,
            topP = topP,
            temperature = temperature,
            maxTokens = maxTokens,
        ).collect { chunk ->
            acc.append(chunk)
            emit(InterpretChunk.Partial(acc.toString()))
        }
        val raw = acc.toString()
        val parsed = parseInterpretation(raw)
            ?: attemptRepair(raw)
            ?: attemptStrictRegenerate(user, photoFile, attempts = 3)
            ?: buildLocalFallback(dreamText, mood, previousSymbols)
        if (parsed == null) emit(InterpretChunk.Failed(raw)) else emit(InterpretChunk.Done(parsed))
    }

    private suspend fun attemptRepair(raw: String): Interpretation? {
        if (raw.isBlank()) return null
        val repaired = StringBuilder()
        llm.generateStream(
            userMessage = InterpretPrompts.repairUser(raw),
            systemPrompt = InterpretPrompts.REPAIR_SYSTEM,
            imageFile = null,
            topK = 40,
            topP = 0.85,
            temperature = 0.25,
            maxTokens = 260,
        ).collect { chunk ->
            repaired.append(chunk)
        }
        return parseInterpretation(repaired.toString())
    }

    private suspend fun attemptStrictRegenerate(user: String, photoFile: File?, attempts: Int): Interpretation? {
        repeat(attempts.coerceAtLeast(1)) {
            val regenerated = StringBuilder()
            llm.generateStream(
                userMessage = user,
                systemPrompt = InterpretPrompts.STRICT_FORMAT_SYSTEM,
                imageFile = photoFile,
                topK = 32,
                topP = 0.8,
                temperature = 0.2,
                maxTokens = 280,
            ).collect { chunk ->
                regenerated.append(chunk)
            }
            parseInterpretation(regenerated.toString())?.let { return it }
        }
        return null
    }

    private fun buildLocalFallback(
        dreamText: String,
        mood: String,
        previousSymbols: List<String>,
    ): Interpretation {
        val tokens = Regex("[A-Za-z]{3,}")
            .findAll(dreamText.lowercase(Locale.ROOT))
            .map { it.value }
            .filterNot { it in STOP_WORDS }
            .distinct()
            .toList()
        val avoid = previousSymbols.map { it.lowercase(Locale.ROOT) }.toSet()
        val fresh = tokens.filterNot { it in avoid }
        val symbols = (fresh + tokens)
            .distinct()
            .take(5)
            .ifEmpty { listOf("water", "night", "memory") }
        val title = symbols.take(3)
            .joinToString(" ")
            .ifBlank { "quiet shifting dream" }
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        val cleanMood = mood.ifBlank { "mixed" }
        val interpretation = buildString {
            append("This dream circles around ")
            append(symbols.joinToString(", "))
            append(", and it reads like your mind is organizing pressure into meaning. ")
            append("The ")
            append(cleanMood)
            append(" tone suggests you may be holding caution and hope at the same time. ")
            append("Track which images repeat and which shift, because that contrast points to what you are ready to face more directly.")
        }
        return Interpretation(
            title = title,
            symbols = symbols,
            interpretation = interpretation,
            intention = "Write one repeating image and one feeling, then choose one small action for today.",
        )
    }

    private companion object {
        val STOP_WORDS = setOf(
            "the", "and", "that", "with", "from", "this", "your", "were", "was", "into",
            "while", "also", "felt", "have", "been", "they", "them", "their", "there",
            "then", "when", "about", "just", "over", "under", "dream", "dreamer",
        )
    }
}
