package com.vijay.tadashi.core.ai.streaming

sealed interface StreamingState {
    data object Idle : StreamingState

    data object Thinking : StreamingState

    data class Streaming(
        val text: String
    ) : StreamingState

    data class Completed(
        val text: String
    ) : StreamingState

    data class Error(
        val message: String
    ) : StreamingState

    data class Cancelled(
        val text: String
    ) : StreamingState
}
