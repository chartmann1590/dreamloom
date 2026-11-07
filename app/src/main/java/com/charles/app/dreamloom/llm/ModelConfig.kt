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

    private val PRIMARY = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/$FILENAME"
    private val MIRROR = "https://huggingface.co/huggingworld/gemma-4-E2B-it-litert-lm/resolve/main/$FILENAME"

    // Direct (non-Play) builds try the mirror first: since Nov 2026 the primary repo serves a newer
    // 2,588,147,712-byte file whose SHA-256 no longer matches the pinned hash (the mirror still has the pinned one).
    /**
     * Direct builds also accept the published SHA-256 of the 2,583,085,056-byte file the mirror serves
     * (Hugging Face x-linked-etag, checked Nov 7 2026) — the size [EXPECTED_SIZE_BYTES] was written for.
     */
    val DIRECT_EXTRA_SHA256: Set<String> = setOf("ab7838cdfc8f77e54d8ca45eadceb20452d9f01e4bfade03e5dce27911b27e42")

    val SOURCES: List<String> =
        if (BuildConfig.STORE == "direct") listOf(MIRROR, PRIMARY) else listOf(PRIMARY, MIRROR)
}
