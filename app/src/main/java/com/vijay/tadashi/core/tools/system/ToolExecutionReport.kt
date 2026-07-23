package com.vijay.tadashi.core.tools.system

import com.vijay.tadashi.core.tools.ToolResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ToolExecutionReport(
    val success: Boolean,
    val verification: Boolean,
    val message: String,
    val executionTime: Long,
    val error: String? = null
)

object ToolExecutionReports {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    fun success(
        message: String,
        verification: Boolean,
        executionTimeMs: Long
    ): ToolResult.Success {
        return ToolResult.Success(
            json.encodeToString(
                ToolExecutionReport(
                    success = true,
                    verification = verification,
                    message = message,
                    executionTime = executionTimeMs,
                    error = null
                )
            )
        )
    }

    fun failure(
        message: String,
        verification: Boolean,
        executionTimeMs: Long,
        error: String? = null
    ): ToolResult.Failure {
        return ToolResult.Failure(
            json.encodeToString(
                ToolExecutionReport(
                    success = false,
                    verification = verification,
                    message = message,
                    executionTime = executionTimeMs,
                    error = error
                )
            )
        )
    }
}
