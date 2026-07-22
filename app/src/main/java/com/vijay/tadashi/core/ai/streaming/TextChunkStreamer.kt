package com.vijay.tadashi.core.ai.streaming

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

interface TextChunkStreamer {
    fun streamText(fullText: String): Flow<StreamingResponse>
}

@Singleton
class SimulatedTextChunkStreamer @Inject constructor() : TextChunkStreamer {
    override fun streamText(fullText: String): Flow<StreamingResponse> {
        return flow {
            val chunkSize = 6
            var index = 0

            while (index < fullText.length) {
                val next = min(index + chunkSize, fullText.length)
                val delta = fullText.substring(index, next)
                emit(StreamingResponse.Chunk(delta))
                index = next
                delay(Random.nextLong(20L, 41L))
            }

            emit(StreamingResponse.Completed(fullText))
        }
    }
}

