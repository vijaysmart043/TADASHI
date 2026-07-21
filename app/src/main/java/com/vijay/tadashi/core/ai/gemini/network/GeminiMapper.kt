package com.vijay.tadashi.core.ai.gemini.network

import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Maps between domain configuration and Gemini request/response payloads.
 */
class GeminiMapper @Inject constructor() {
    /**
     * Builds the official Gemini "generateContent" request.
     */
    fun toRequest(
        input: String,
        configuration: AIConfiguration
    ): GeminiRequest {
        val parts = JsonArray(listOf(JsonObject(mapOf("text" to JsonPrimitive(input)))))

        val content = JsonObject(
            mapOf(
                "role" to JsonPrimitive("user"),
                "parts" to parts
            )
        )

        val contents = JsonArray(listOf(content))

        val generationConfig = JsonObject(
            mapOf(
                "temperature" to JsonPrimitive(configuration.temperature),
                "maxOutputTokens" to JsonPrimitive(configuration.maxTokens)
            )
        )

        val root = JsonObject(
            mapOf(
                "contents" to contents,
                "generationConfig" to generationConfig
            )
        )

        return GeminiRequest(json = root)
    }

    /**
     * Extracts assistant text and token usage (when available).
     */
    fun toResult(
        response: GeminiResponse
    ): AIResult {
        val text = extractText(response.json)
        val tokensUsed = extractTokenUsage(response.json)

        return if (text.isNullOrBlank()) {
            AIResult(
                text = "",
                success = false,
                error = "Gemini returned an empty response",
                tokensUsed = tokensUsed,
                provider = AIProvider.GEMINI
            )
        } else {
            AIResult(
                text = text,
                success = true,
                tokensUsed = tokensUsed,
                provider = AIProvider.GEMINI
            )
        }
    }

    private fun extractText(root: JsonObject): String? {
        val candidates = root["candidates"] as? JsonArray ?: return null
        val firstCandidate = candidates.firstOrNull()?.jsonObject ?: return null
        val content = firstCandidate["content"]?.jsonObject ?: return null
        val parts = content["parts"]?.jsonArray ?: return null
        val firstPart = parts.firstOrNull()?.jsonObject ?: return null
        return firstPart["text"]?.jsonPrimitive?.contentOrNull
    }

    private fun extractTokenUsage(root: JsonObject): Int? {
        val usage = root["usageMetadata"]?.jsonObject ?: return null
        return usage["totalTokenCount"]?.jsonPrimitive?.intOrNull
    }

    private val JsonPrimitive.contentOrNull: String?
        get() = runCatching { content }.getOrNull()
}

