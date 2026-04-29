package com.charles.app.dreamloom.llm

import com.charles.app.dreamloom.llm.prompts.InsightOraclePrompts
import kotlinx.coroutines.flow.collect

class OracleResponder(
    private val engine: LlmEngine,
) {
    suspend fun answer(
        question: String,
        topSymbols: List<Pair<String, Int>>,
    ): String {
        val prompt = InsightOraclePrompts.oracleUser(topSymbols, question)
        val primary = StringBuilder()
        generate(
            prompt = prompt,
            systemPrompt = InsightOraclePrompts.ORACLE_SYSTEM,
            topK = GenParams.ORACLE_TOP_K,
            topP = GenParams.ORACLE_TOP_P.toDouble(),
            temperature = GenParams.ORACLE_TEMPERATURE.toDouble(),
            maxTokens = GenParams.ORACLE_MAX_TOKENS,
            sink = primary,
        )
        val cleanedPrimary = OracleAnswerPolicy.clean(primary.toString())
        if (OracleAnswerPolicy.isAcceptable(cleanedPrimary)) return cleanedPrimary

        repeat(2) {
            val strict = StringBuilder()
            generate(
                prompt = prompt,
                systemPrompt = InsightOraclePrompts.ORACLE_STRICT_SYSTEM,
                topK = 40,
                topP = 0.85,
                temperature = 0.25,
                maxTokens = 180,
                sink = strict,
            )
            val cleanedStrict = OracleAnswerPolicy.clean(strict.toString())
            if (OracleAnswerPolicy.isAcceptable(cleanedStrict)) return cleanedStrict
        }
        return OracleAnswerPolicy.fallback(question, topSymbols)
    }

    private suspend fun generate(
        prompt: String,
        systemPrompt: String,
        topK: Int,
        topP: Double,
        temperature: Double,
        maxTokens: Int,
        sink: StringBuilder,
    ) {
        engine.generateStream(
            userMessage = prompt,
            systemPrompt = systemPrompt,
            topK = topK,
            topP = topP,
            temperature = temperature,
            maxTokens = maxTokens,
        ).collect { sink.append(it) }
    }
}
