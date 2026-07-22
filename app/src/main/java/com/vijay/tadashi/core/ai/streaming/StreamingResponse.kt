package com.vijay.tadashi.core.ai.streaming

sealed interface StreamingResponse {
    data class Chunk(
        val textDelta: String
    ) : StreamingResponse

    data class Completed(
        val fullText: String
    ) : StreamingResponse

    data class Error(
        val message: String
    ) : StreamingResponse
}

