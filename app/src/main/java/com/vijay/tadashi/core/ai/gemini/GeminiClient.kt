package com.vijay.tadashi.core.ai.gemini

import com.vijay.tadashi.core.ai.AIConfiguration
import org.json.JSONObject
import javax.inject.Inject

/**
 * Builds request JSON for Gemini "generate content" style APIs.
 */
class GeminiRequestBuilder @Inject constructor() {
    /**
     * Creates a request body for a single-turn prompt.
     */
    fun buildGenerateContentRequest(
        input: String,
        configuration: AIConfiguration
    ): JSONObject {
        val textPart = JSONObject()
            .put("text", input)

        val parts = org.json.JSONArray()
            .put(textPart)

        val content = JSONObject()
            .put("parts", parts)

        val contents = org.json.JSONArray()
            .put(content)

        val generationConfig = JSONObject()
            .put("temperature", configuration.temperature)
            .put("maxOutputTokens", configuration.maxTokens)

        return JSONObject()
            .put("contents", contents)
            .put("generationConfig", generationConfig)
    }
}

/**
 * Parses a Gemini response payload into a usable assistant text.
 *
 * This class is intentionally tolerant; providers can vary fields slightly between versions.
 */
class GeminiResponseParser @Inject constructor() {
    /**
     * Extracts assistant text if present; otherwise returns null.
     */
    fun parseText(responseJson: JSONObject): String? {
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null

        val firstPart = parts.optJSONObject(0) ?: return null
        return firstPart.optString("text", null)
    }
}

/**
 * Prepared HTTP client abstraction for Gemini API calls.
 *
 * Networking is intentionally not executed in Phase 3.1.
 */
class GeminiHttpClient @Inject constructor() {
    /**
     * Placeholder for a future POST call.
     */
    suspend fun postJson(
        url: String,
        apiKey: String,
        body: JSONObject
    ): JSONObject {
        return JSONObject()
    }
}

