package com.vijay.tadashi.core.ai.streaming

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingParser @Inject constructor(
    private val json: Json
) {
    fun parseSseLine(line: String): StreamingResponse? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("data:")) return null
        val payload = trimmed.removePrefix("data:").trim()
        if (payload.isBlank() || payload == "[DONE]") return null

        return runCatching {
            val root = json.parseToJsonElement(payload).jsonObject
            val delta = root["delta"]?.jsonObject
            val textDelta = delta?.get("text")?.jsonPrimitive?.content
            if (textDelta.isNullOrBlank()) null else StreamingResponse.Chunk(textDelta)
        }.getOrNull()
    }
}

