package com.vijay.tadashi.core.tools.common

import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.system.ToolExecutionReport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object ToolExecutionReportDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun decode(result: ToolResult): ToolExecutionReport? {
        val raw = when (result) {
            is ToolResult.Success -> result.message
            is ToolResult.Failure -> result.message
            else -> return null
        }
        return runCatching { json.decodeFromString<ToolExecutionReport>(raw) }.getOrNull()
    }
}

