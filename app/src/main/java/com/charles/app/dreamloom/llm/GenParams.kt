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
}
