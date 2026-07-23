package com.vijay.tadashi.core.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolParser @Inject constructor(
    private val json: Json
) {
    fun parsePlan(
        structuredOutput: String,
        caller: String,
        timestampMs: Long = System.currentTimeMillis()
    ): List<PlannedToolStep>? {
        val trimmed = structuredOutput.trim()
        if (trimmed.isBlank()) return null

        val root = runCatching { json.parseToJsonElement(trimmed).jsonObject }.getOrNull() ?: return null
        val stepsJson = root["steps"]?.jsonArray
        if (stepsJson != null) {
            val steps = stepsJson.mapIndexedNotNull { index, el ->
                val obj = runCatching { el.jsonObject }.getOrNull() ?: return@mapIndexedNotNull null
                val toolId = obj["tool"]?.jsonPrimitive?.content
                    ?: obj["toolId"]?.jsonPrimitive?.content
                    ?: return@mapIndexedNotNull null

                val order = obj["order"]?.jsonPrimitive?.content?.toIntOrNull() ?: (index + 1)
                val optional = obj["optional"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                val intent = obj["intent"]?.jsonPrimitive?.content
                val confidence = obj["confidence"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val reason = obj["reason"]?.jsonPrimitive?.content

                val argumentsJson = obj["arguments"]?.jsonObject
                val arguments = buildMap {
                    argumentsJson?.forEach { (key, value) ->
                        val content = runCatching { value.jsonPrimitive.content }.getOrNull()
                        put(key, content ?: value.toString())
                    }
                }

                PlannedToolStep(
                    order = order,
                    request = ToolRequest(
                        toolId = toolId,
                        arguments = arguments,
                        caller = caller,
                        timestampMs = timestampMs
                    ),
                    optional = optional,
                    intent = intent,
                    confidence = confidence,
                    reason = reason
                )
            }.sortedBy { it.order }

            if (steps.isNotEmpty()) {
                ToolsLogger.d("Plan parsed: ${steps.size} steps")
                return steps
            }
        }

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
        val single = ToolRequest(
            toolId = toolId,
            arguments = arguments,
            caller = caller,
            timestampMs = timestampMs
        )
        return listOf(
            PlannedToolStep(
                order = 1,
                request = single
            )
        )
    }

    fun parse(
        structuredOutput: String,
        caller: String,
        timestampMs: Long = System.currentTimeMillis()
    ): ToolRequest? {
        val planned = parsePlan(
            structuredOutput = structuredOutput,
            caller = caller,
            timestampMs = timestampMs
        ) ?: return null

        return planned.firstOrNull()?.request
    }
}
