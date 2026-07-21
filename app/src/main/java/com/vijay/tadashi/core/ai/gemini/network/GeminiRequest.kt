package com.vijay.tadashi.core.ai.gemini.network

import kotlinx.serialization.json.JsonObject

/**
 * Wrapper for a Gemini request JSON payload.
 *
 * Kotlin Serialization is used via [JsonObject] to avoid hardcoding models and to tolerate evolving
 * server fields without risking crashes.
 */
data class GeminiRequest(
    val json: JsonObject
)

