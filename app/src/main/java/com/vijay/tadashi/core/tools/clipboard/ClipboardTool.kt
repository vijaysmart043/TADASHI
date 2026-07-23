package com.vijay.tadashi.core.tools.clipboard

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
class ClipboardTool @Inject constructor(
    private val controller: ClipboardController
) : Tool {
    override val id: String = "CLIPBOARD"
    override val displayName: String = "Clipboard"
    override val description: String = "Clipboard actions: COPY, READ, CLEAR, SHARE."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.CLIPBOARD

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "READ"

        ToolsLogger.d("Clipboard tool: action=$action args=${request.arguments}")

        val result = when (action) {
            "COPY" -> copy(request, startMs)
            "READ" -> read(startMs)
            "CLEAR" -> clear(startMs)
            "SHARE" -> share(request, startMs)
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
        ToolsLogger.d("Clipboard tool finished: action=$action timeMs=$elapsed payload=$payload")
        return result
    }

    private fun copy(request: ToolRequest, startMs: Long): ToolResult {
        val text = request.arguments["text"] ?: request.arguments["value"]
        if (text.isNullOrBlank()) {
            return ToolExecutionReports.failure(
                message = "Missing argument: text",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "MISSING_ARGUMENT"
            )
        }

        val result = controller.copy(text).getOrElse { e ->
            return ToolExecutionReports.failure(
                message = "Failed to copy to clipboard",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = e.message ?: e::class.java.simpleName
            )
        }

        val elapsed = System.currentTimeMillis() - startMs
        return ToolExecutionReports.success(
            message = "Copied to clipboard",
            verification = result == Unit,
            executionTimeMs = elapsed
        )
    }

    private fun read(startMs: Long): ToolResult {
        val result = controller.read()
        val elapsed = System.currentTimeMillis() - startMs

        if (result.restricted) {
            return ToolExecutionReports.failure(
                message = result.message,
                verification = false,
                executionTimeMs = elapsed,
                error = result.error
            )
        }

        val text = result.text
        return if (text != null) {
            ToolExecutionReports.success(
                message = text,
                verification = true,
                executionTimeMs = elapsed
            )
        } else {
            ToolExecutionReports.failure(
                message = result.message,
                verification = false,
                executionTimeMs = elapsed,
                error = result.error ?: "EMPTY"
            )
        }
    }

    private fun clear(startMs: Long): ToolResult {
        controller.clear().getOrElse { e ->
            return ToolExecutionReports.failure(
                message = "Failed to clear clipboard",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = e.message ?: e::class.java.simpleName
            )
        }

        return ToolExecutionReports.success(
            message = "Clipboard cleared",
            verification = true,
            executionTimeMs = System.currentTimeMillis() - startMs
        )
    }

    private fun share(request: ToolRequest, startMs: Long): ToolResult {
        val requestedText = request.arguments["text"] ?: request.arguments["value"]
        val text = if (!requestedText.isNullOrBlank()) {
            requestedText
        } else {
            controller.read().text
        }

        if (text.isNullOrBlank()) {
            return ToolExecutionReports.failure(
                message = "Clipboard is empty",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "EMPTY"
            )
        }

        controller.share(text).getOrElse { e ->
            return ToolExecutionReports.failure(
                message = "Failed to share clipboard",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = e.message ?: e::class.java.simpleName
            )
        }

        return ToolExecutionReports.success(
            message = "Sharing clipboard content",
            verification = true,
            executionTimeMs = System.currentTimeMillis() - startMs
        )
    }
}
