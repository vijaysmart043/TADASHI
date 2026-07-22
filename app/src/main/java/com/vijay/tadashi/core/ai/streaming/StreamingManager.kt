package com.vijay.tadashi.core.ai.streaming

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingManager @Inject constructor() {
    private val _state = MutableStateFlow<StreamingState>(StreamingState.Idle)
    val state: StateFlow<StreamingState> = _state.asStateFlow()

    private var streamingJob: Job? = null

    fun start(
        scope: CoroutineScope,
        stream: Flow<StreamingResponse>
    ) {
        streamingJob?.cancel()
        _state.value = StreamingState.Thinking
        Log.d(TAG, "Streaming started")

        streamingJob = scope.launch {
            var buffer = ""
            var chunkIndex = 0
            try {
                stream.collect { event ->
                    when (event) {
                        is StreamingResponse.Chunk -> {
                            buffer += event.textDelta
                            _state.value = StreamingState.Streaming(buffer)
                            chunkIndex += 1
                            Log.d(
                                TAG,
                                "Chunk emitted (chunkIndex=$chunkIndex, deltaSize=${event.textDelta.length}, totalSize=${buffer.length})"
                            )
                        }

                        is StreamingResponse.Completed -> {
                            buffer = event.fullText
                            _state.value = StreamingState.Completed(buffer)
                            Log.d(TAG, "Streaming finished (totalSize=${buffer.length})")
                        }

                        is StreamingResponse.Error -> {
                            _state.value = StreamingState.Error(event.message)
                            Log.d(TAG, "Streaming finished (error)")
                        }
                    }
                }
            } catch (e: CancellationException) {
                _state.value = StreamingState.Cancelled(buffer)
                Log.d(TAG, "Cancelled (totalSize=${buffer.length}, chunks=$chunkIndex)")
                throw e
            } catch (e: Exception) {
                _state.value = StreamingState.Error("Streaming failed")
                Log.d(TAG, "Streaming finished (exception)", e)
            }
        }
    }

    fun cancel() {
        Log.d(TAG, "Cancelled (requested)")
        streamingJob?.cancel()
    }

    fun reset() {
        streamingJob?.cancel()
        streamingJob = null
        _state.value = StreamingState.Idle
    }

    private companion object {
        private const val TAG = "TADASHI-STREAM"
    }
}
