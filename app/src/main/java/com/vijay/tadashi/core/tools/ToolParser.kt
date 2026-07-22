package com.vijay.tadashi.core.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolParser @Inject constructor(
    private val json: Json
) {
    fun parse(
        structuredOutput: String,
        caller: String,
        timestampMs: Long = System.currentTimeMillis()
    ): ToolRequest? {
        val trimmed = structuredOutput.trim()
        if (trimmed.isBlank()) return null

        val root = runCatching { json.parseToJsonElement(trimmed).jsonObject }.getOrNull() ?: return null
        val toolId = root["tool"]?.jsonPrimitive?.content
            ?: root["toolId"]?.jsonPrimitive?.content
            ?: return null

        val argumentsJson = root["arguments"]?.jsonObject
        val arguments = buildMap {
            argumentsJson?.forEach { (key, value) ->
                val content = runCatching { value.jsonPrimitive.content }.getOrNull()
                put(key, content ?: value.toString())
            }
        }

        ToolsLogger.d("Tool parsed: $toolId")
        return ToolRequest(
            toolId = toolId,
            arguments = arguments,
            caller = caller,
            timestampMs = timestampMs
        )
    }
}
