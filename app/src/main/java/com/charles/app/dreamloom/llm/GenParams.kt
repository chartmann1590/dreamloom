package com.charles.app.dreamloom.llm

/**
 * Generation parameters mirror [spec/PROMPTS.md] and [tech/GEMMA_INTEGRATION.md].
 * LiteRT-LM uses [com.google.ai.edge.litertlm.GenerationConfig] (package name may vary by version).
 */
object GenParams {
    // Populated in [LlmEngine] using LiteRT-LM's builder; kept as documentation constants.
    const val INTERPRET_TEMPERATURE = 0.85f
    const val INTERPRET_TOP_P = 0.95f
    const val INTERPRET_TOP_K = 64
    const val INTERPRET_MAX_TOKENS = 320

    const val WEEKLY_INSIGHT_TEMPERATURE = 0.7f
    const val WEEKLY_INSIGHT_TOP_P = 0.9f
    const val WEEKLY_INSIGHT_TOP_K = 40
    const val WEEKLY_INSIGHT_MAX_TOKENS = 300

    const val ORACLE_TEMPERATURE = 0.85f
    const val ORACLE_TOP_P = 0.95f
    const val ORACLE_TOP_K = 64
    const val ORACLE_MAX_TOKENS = 220
}
