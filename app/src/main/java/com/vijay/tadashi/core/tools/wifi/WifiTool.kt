package com.vijay.tadashi.core.tools.wifi

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import com.vijay.tadashi.core.tools.system.ToolExecutionReports
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiTool @Inject constructor(
    private val controller: WifiController
) : Tool {
    override val id: String = "WIFI"
    override val displayName: String = "Wi-Fi"
    override val description: String = "Controls Wi-Fi. Actions: ON, OFF, TOGGLE, STATUS."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "STATUS"

        ToolsLogger.d("WiFi tool: action=$action args=${request.arguments}")

        val result = when (action) {
            "STATUS" -> status(startMs)
            "ON" -> setEnabled(true, startMs)
            "OFF" -> setEnabled(false, startMs)
            "TOGGLE" -> toggle(startMs)
            else -> ToolExecutionReports.failure(
                message = "Unsupported action: $action",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "UNSUPPORTED_ACTION"
            )
        }

        val elapsed = System.currentTimeMillis() - startMs
        val payload = when (result) {
            is ToolResult.Success -> result.message
            is ToolResult.Failure -> result.message
            is ToolResult.Unsupported -> result.message
            is ToolResult.Cancelled -> result.message
            is ToolResult.PermissionDenied -> null
        }
        ToolsLogger.d("WiFi tool finished: action=$action timeMs=$elapsed payload=$payload")
        return result
    }

    private fun status(startMs: Long): ToolResult {
        val enabled = controller.isEnabled().getOrElse { e ->
            return ToolExecutionReports.failure(
                message = "Failed to read Wi-Fi status",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = e.message ?: e::class.java.simpleName
            )
        }

        return ToolExecutionReports.success(
            message = if (enabled) "Wi-Fi is ON" else "Wi-Fi is OFF",
            verification = true,
            executionTimeMs = System.currentTimeMillis() - startMs
        )
    }

    private fun toggle(startMs: Long): ToolResult {
        val current = controller.isEnabled().getOrElse { e ->
            return ToolExecutionReports.failure(
                message = "Failed to read Wi-Fi status",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = e.message ?: e::class.java.simpleName
            )
        }

        return setEnabled(!current, startMs)
    }

    private fun setEnabled(enabled: Boolean, startMs: Long): ToolResult {
        val outcome = controller.setEnabled(enabled)

        val elapsed = System.currentTimeMillis() - startMs
        if (outcome.requiresUserAction) {
            return ToolExecutionReports.failure(
                message = outcome.message,
                verification = outcome.verification,
                executionTimeMs = elapsed,
                error = outcome.error
            )
        }

        if (outcome.enabled != enabled) {
            return ToolExecutionReports.failure(
                message = outcome.message,
                verification = false,
                executionTimeMs = elapsed,
                error = outcome.error
            )
        }

        return ToolExecutionReports.success(
            message = outcome.message,
            verification = outcome.verification,
            executionTimeMs = elapsed
        )
    }
}
