package com.vijay.tadashi.core.ai.gemini.network

import kotlinx.serialization.json.JsonObject

/**
 * Wrapper for a Gemini response JSON payload.
 */
data class GeminiResponse(
    val json: JsonObject
)

