package com.vijay.tadashi.core.tools.bluetooth

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
class BluetoothTool @Inject constructor(
    private val controller: BluetoothController
) : Tool {
    override val id: String = "BLUETOOTH"
    override val displayName: String = "Bluetooth"
    override val description: String = "Controls Bluetooth. Actions: ON, OFF, TOGGLE, STATUS."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "STATUS"

        ToolsLogger.d("Bluetooth tool: action=$action args=${request.arguments}")

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
        ToolsLogger.d("Bluetooth tool finished: action=$action timeMs=$elapsed payload=$payload")
        return result
    }

    private fun status(startMs: Long): ToolResult {
        val status = controller.status()
        val elapsed = System.currentTimeMillis() - startMs

        if (status.requiresPermission) {
            return ToolExecutionReports.failure(
                message = "${status.message}. Please grant the permission and try again.",
                verification = false,
                executionTimeMs = elapsed,
                error = status.requiredPermission ?: status.error
            )
        }

        if (status.enabled == null) {
            return ToolExecutionReports.failure(
                message = status.message,
                verification = false,
                executionTimeMs = elapsed,
                error = status.error
            )
        }

        return ToolExecutionReports.success(
            message = status.message,
            verification = true,
            executionTimeMs = elapsed
        )
    }

    private fun toggle(startMs: Long): ToolResult {
        val status = controller.status()
        if (status.requiresPermission || status.enabled == null) {
            val elapsed = System.currentTimeMillis() - startMs
            return ToolExecutionReports.failure(
                message = status.message,
                verification = false,
                executionTimeMs = elapsed,
                error = status.requiredPermission ?: status.error
            )
        }

        return setEnabled(!status.enabled, startMs)
    }

    private fun setEnabled(enabled: Boolean, startMs: Long): ToolResult {
        val outcome = controller.setEnabled(enabled)
        val elapsed = System.currentTimeMillis() - startMs

        if (outcome.requiresPermission) {
            return ToolExecutionReports.failure(
                message = "${outcome.message}. Please grant the permission and try again.",
                verification = false,
                executionTimeMs = elapsed,
                error = outcome.requiredPermission ?: outcome.error
            )
        }

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
