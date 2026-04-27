package com.charles.app.dreamloom.llm

object ModelConfig {
    const val VERSION = "v1"
    const val FILENAME = "gemma-4-E2B-it-int4.litertlm"
    const val EXPECTED_SIZE_BYTES = 2_770_000_000L
    const val SHA256 = "REPLACE_AT_BUILD_TIME"

    val SOURCES: List<String> = listOf(
        "https://github.com/REPLACE_USER/dreamloom-android/releases/download/model-$VERSION/$FILENAME",
        "https://r2.dreamloom.app/model-$VERSION/$FILENAME",
    )
}
