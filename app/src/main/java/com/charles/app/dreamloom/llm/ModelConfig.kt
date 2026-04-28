package com.charles.app.dreamloom.llm

import com.charles.app.dreamloom.BuildConfig

object ModelConfig {
    const val VERSION = "v1"
    const val FILENAME = "gemma-4-E2B-it.litertlm"

    /** Shown in Settings → About (matches product spec wording). */
    const val ABOUT_LABEL = "Gemma 4 E2B-IT, int4, May 2026"
    const val EXPECTED_SIZE_BYTES = 2_583_085_056L

    /** Expected SHA-256 of [FILENAME] after download (`BuildConfig.MODEL_SHA256`). */
    val modelSha256: String get() = BuildConfig.MODEL_SHA256

    val SOURCES: List<String> = listOf(
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/$FILENAME",
        "https://huggingface.co/huggingworld/gemma-4-E2B-it-litert-lm/resolve/main/$FILENAME",
    )
}
