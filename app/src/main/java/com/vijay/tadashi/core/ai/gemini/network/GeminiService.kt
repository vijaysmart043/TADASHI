package com.vijay.tadashi.core.ai.gemini.network

import android.util.Log
import com.vijay.tadashi.core.ai.AIConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val mapper: GeminiMapper
) {
    /**
     * Executes a Gemini "generateContent" request for the configured model.
     *
     * This method never throws; it returns [GeminiServiceResult] representing success/failure.
     */
    suspend fun generateContent(
        input: String,
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

        val request = mapper.toRequest(
            input = input,
            configuration = configuration
        )

        val requestJson = json.encodeToString(JsonObject.serializer(), request.json)
        val requestBody = requestJson.toRequestBody(JSON_MEDIA_TYPE)

        val startMs = System.currentTimeMillis()
        Log.d(TAG, "Request started (model=$modelName)")

        return try {
            val response = api.generateContent(
                model = modelName,
                apiKey = apiKey,
                body = requestBody
            )

            val elapsedMs = System.currentTimeMillis() - startMs
            Log.d(TAG, "Request finished (status=${response.code()}, timeMs=$elapsedMs)")

            if (!response.isSuccessful) {
                val errorMessage = errorMessageForHttpCode(response.code())
                Log.d(TAG, "HTTP error (code=${response.code()}): $errorMessage")
                return GeminiServiceResult.Error(errorMessage)
            }

            val bodyString = response.body()?.string()
            if (bodyString.isNullOrBlank()) {
                return GeminiServiceResult.Error("Gemini returned an empty response body")
            }

            val rootElement = json.parseToJsonElement(bodyString)
            val rootObject = rootElement.jsonObject

            GeminiServiceResult.Success(
                response = GeminiResponse(json = rootObject)
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
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

/**
 * Result of a GeminiService call.
 */
sealed interface GeminiServiceResult {
    data class Success(
        val response: GeminiResponse
    ) : GeminiServiceResult

    data class Error(
        val message: String
    ) : GeminiServiceResult
}

