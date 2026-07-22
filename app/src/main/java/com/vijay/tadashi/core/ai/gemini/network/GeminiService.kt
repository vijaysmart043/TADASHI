package com.vijay.tadashi.core.ai.gemini.network

import android.util.Log
import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.streaming.TextChunkStreamer
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Service responsible for executing Gemini requests and returning parsed results.
 */
class GeminiService @Inject constructor(
    private val api: GeminiApi,
    private val json: Json,
    private val mapper: GeminiMapper,
    private val textChunkStreamer: TextChunkStreamer
) {
    /**
     * Executes a Gemini "generateContent" request for the configured model.
     *
     * This method never throws; it returns [GeminiServiceResult] representing success/failure.
     */
    suspend fun generateContent(
        request: GeminiGenerateContentRequest,
        configuration: AIConfiguration
    ): GeminiServiceResult {
        val apiKey = configuration.apiKey.trim()
        if (apiKey.isBlank()) {
            return GeminiServiceResult.Error("Gemini API key is missing")
        }

        val modelName = configuration.modelName.trim()
        if (modelName.isBlank()) {
            return GeminiServiceResult.Error("Gemini model name is missing")
        }

        val startMs = System.currentTimeMillis()
        Log.d(TAG, "Request started (model=$modelName)")

        return try {
            val response = api.generateContent(
                model = modelName,
                apiKey = apiKey,
                body = request
            )

            val elapsedMs = System.currentTimeMillis() - startMs
            Log.d(TAG, "Request finished (status=${response.code()}, timeMs=$elapsedMs)")

            if (!response.isSuccessful) {
                val errorMessage = errorMessageForHttpCode(response.code())
                val serverMessage = response.errorBody()?.string()
                val parsedServerMessage = parseServerErrorMessage(serverMessage)
                val message = if (parsedServerMessage.isNullOrBlank()) {
                    errorMessage
                } else {
                    "$errorMessage: $parsedServerMessage"
                }
                Log.d(TAG, "HTTP error (code=${response.code()}): $message")
                return GeminiServiceResult.Error(message)
            }

            val body = response.body()
                ?: return GeminiServiceResult.Error("Gemini returned an empty response body")

            GeminiServiceResult.Success(
                response = body
            )
        } catch (e: UnknownHostException) {
            Log.d(TAG, "No internet / DNS error", e)
            GeminiServiceResult.Error("No internet connection")
        } catch (e: ConnectException) {
            Log.d(TAG, "Connection error", e)
            GeminiServiceResult.Error("Unable to connect to Gemini")
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, "Timeout", e)
            GeminiServiceResult.Error("Gemini request timed out")
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.d(TAG, "JSON parse error", e)
            GeminiServiceResult.Error("Gemini response could not be parsed")
        } catch (e: IOException) {
            Log.d(TAG, "Network I/O error", e)
            GeminiServiceResult.Error("Network error while calling Gemini")
        } catch (e: Exception) {
            Log.d(TAG, "Unknown error", e)
            GeminiServiceResult.Error("Unknown error while calling Gemini")
        }
    }

    fun generateContentStream(
        request: GeminiGenerateContentRequest,
        configuration: AIConfiguration
    ): Flow<StreamingResponse> {
        return flow {
            Log.d(STREAM_TAG, "Streaming started")

            try {
                val result = generateContent(
                    request = request,
                    configuration = configuration
                )

                when (result) {
                    is GeminiServiceResult.Error -> {
                        emit(StreamingResponse.Error(result.message))
                        return@flow
                    }

                    is GeminiServiceResult.Success -> {
                        val fullText = mapper.extractText(result.response)
                        if (fullText.isNullOrBlank()) {
                            emit(StreamingResponse.Error("Gemini returned an empty response"))
                            return@flow
                        }

                        emitAll(
                            textChunkStreamer.streamText(fullText)
                        )

                        Log.d(STREAM_TAG, "Streaming finished (totalSize=${fullText.length})")
                    }
                }
            } catch (e: CancellationException) {
                Log.d(STREAM_TAG, "Cancelled")
                throw e
            }
        }
    }

    private fun parseServerErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null

        return runCatching {
            val root = json.parseToJsonElement(errorBody).jsonObject
            val error = root["error"]?.jsonObject
            error?.get("message")?.toString()?.trim('"')
        }.getOrNull()
    }

    private fun errorMessageForHttpCode(code: Int): String {
        return when (code) {
            401 -> "Unauthorized (check your Gemini API key)"
            403 -> "Forbidden (Gemini access denied)"
            429 -> "Rate limit exceeded. Please try again later"
            in 500..599 -> "Gemini server error. Please try again later"
            else -> "Gemini error (HTTP $code)"
        }
    }

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
        private const val STREAM_TAG = "TADASHI-STREAM"
    }
}

/**
 * Result of a GeminiService call.
 */
sealed interface GeminiServiceResult {
    data class Success(
        val response: GeminiGenerateContentResponse
    ) : GeminiServiceResult

    data class Error(
        val message: String
    ) : GeminiServiceResult
}
