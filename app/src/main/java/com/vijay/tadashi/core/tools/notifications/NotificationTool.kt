package com.vijay.tadashi.core.tools.notifications

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import com.vijay.tadashi.core.tools.common.ControllerResult
import com.vijay.tadashi.core.tools.common.ToolExecutionReportDecoder
import com.vijay.tadashi.core.tools.system.ToolExecutionReports
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTool @Inject constructor(
    private val controller: NotificationController
) : Tool {
    override val id: String = "NOTIFICATIONS"
    override val displayName: String = "Notifications"
    override val description: String =
        "Reads and clears active notifications (requires Notification Access). Actions: READ, COUNT, LATEST, CLEAR. Optional filter: app/package."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.OTHER

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "READ"
        val filter = request.arguments["app"]?.trim()
            ?: request.arguments["package"]?.trim()

        ToolsLogger.d("Notification tool: action=$action filter=$filter args=${request.arguments}")

        val result = when (action) {
            "READ" -> read(filter, startMs)
            "COUNT" -> count(filter, startMs)
            "LATEST" -> latest(filter, startMs)
            "CLEAR" -> clear(filter, startMs)
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
        val report = ToolExecutionReportDecoder.decode(result)
        ToolsLogger.d(
            "Notification tool finished: action=$action timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
        )
        return result
    }

    private fun read(filter: String?, startMs: Long): ToolResult {
        return when (val r = controller.readActiveNotifications(filter)) {
            is ControllerResult.Success -> {
                val list = r.value
                val message = if (list.isEmpty()) {
                    "No active notifications"
                } else {
                    buildString {
                        append("You have ${list.size} notifications")
                        list.take(6).forEach { n ->
                            append("\n- ${n.packageName}: ${n.title ?: "Notification"}")
                            n.text?.let { append(" — $it") }
                        }
                        if (list.size > 6) append("\n(and ${list.size - 6} more)")
                    }
                }
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = System.currentTimeMillis() - startMs
                )
            }
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "READ_FAILED"
            )
        }
    }

    private fun count(filter: String?, startMs: Long): ToolResult {
        return when (val r = controller.readActiveNotifications(filter)) {
            is ControllerResult.Success -> {
                val count = r.value.size
                val message = if (filter.isNullOrBlank()) {
                    "You have $count notifications"
                } else {
                    "You have $count notifications for $filter"
                }
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = System.currentTimeMillis() - startMs
                )
            }
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "COUNT_FAILED"
            )
        }
    }

    private fun latest(filter: String?, startMs: Long): ToolResult {
        return when (val r = controller.readActiveNotifications(filter)) {
            is ControllerResult.Success -> {
                val n = r.value.firstOrNull()
                val message = if (n == null) {
                    "No active notifications"
                } else {
                    buildString {
                        append("Latest notification from ${n.packageName}")
                        n.title?.let { append(": $it") }
                        n.text?.let { append(" — $it") }
                    }
                }
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = System.currentTimeMillis() - startMs
                )
            }
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "LATEST_FAILED"
            )
        }
    }

    private fun clear(filter: String?, startMs: Long): ToolResult {
        return when (val r = controller.clearDismissibleNotifications(filter)) {
            is ControllerResult.Success -> {
                val message = if (filter.isNullOrBlank()) {
                    "Cleared ${r.value} notifications"
                } else {
                    "Cleared ${r.value} notifications for $filter"
                }
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = System.currentTimeMillis() - startMs
                )
            }
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "CLEAR_FAILED"
            )
        }
    }
}
