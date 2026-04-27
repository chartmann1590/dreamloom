package com.charles.app.dreamloom.llm

sealed class LlmEngineState {
    data object NotLoaded : LlmEngineState()
    data class Loading(val progress: Float) : LlmEngineState()
    data object Ready : LlmEngineState()
    data class Error(val cause: Throwable) : LlmEngineState()
}
