package com.vijay.tadashi.core.tools.battery

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
class BatteryTool @Inject constructor(
    private val provider: BatteryInfoProvider
) : Tool {
    override val id: String = "BATTERY"
    override val displayName: String = "Battery"
    override val description: String =
        "Reads battery information. Actions: PERCENTAGE, CHARGING, HEALTH, TEMPERATURE, POWER_SAVER, STATUS."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "STATUS"

        ToolsLogger.d("Battery tool: action=$action args=${request.arguments}")

        val result = run {
            val info = provider.getBatteryInfo().getOrElse { e ->
                return@run ToolExecutionReports.failure(
                    message = "Failed to read battery information",
                    verification = false,
                    executionTimeMs = System.currentTimeMillis() - startMs,
                    error = e.message ?: e::class.java.simpleName
                )
            }

            val message = when (action) {
                "PERCENTAGE" -> info.percentage?.let { "Battery is $it%" } ?: "Battery percentage unavailable"
                "CHARGING" -> info.isCharging?.let { if (it) "Battery is charging" else "Battery is not charging" }
                    ?: "Charging state unavailable"
                "HEALTH" -> info.health?.let { "Battery health: $it" } ?: "Battery health unavailable"
                "TEMPERATURE" -> info.temperatureC?.let { "Battery temperature: $it°C" }
                    ?: "Battery temperature unavailable"
                "POWER_SAVER" -> info.powerSave?.let { if (it) "Power saver is ON" else "Power saver is OFF" }
                    ?: "Power saver status unavailable"
                "STATUS" -> buildString {
                    append(info.percentage?.let { "Battery: $it%" } ?: "Battery: unknown")
                    info.isCharging?.let { append(if (it) ", charging" else ", not charging") }
                    info.health?.let { append(", health $it") }
                    info.temperatureC?.let { append(", $it°C") }
                    info.powerSave?.let { append(if (it) ", power saver ON" else ", power saver OFF") }
                }
                else -> null
            } ?: return@run ToolExecutionReports.failure(
                message = "Unsupported action: $action",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "UNSUPPORTED_ACTION"
            )

            val success = !message.endsWith("unavailable") && !message.contains("unknown")
            val elapsed = System.currentTimeMillis() - startMs

            if (success) {
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = elapsed
                )
            } else {
                ToolExecutionReports.failure(
                    message = message,
                    verification = false,
                    executionTimeMs = elapsed,
                    error = "DATA_UNAVAILABLE"
                )
            }
        }

        val elapsed = System.currentTimeMillis() - startMs
        val payload = when (result) {
            is ToolResult.Success -> result.message
            is ToolResult.Failure -> result.message
            is ToolResult.Unsupported -> result.message
            is ToolResult.Cancelled -> result.message
            is ToolResult.PermissionDenied -> null
        }
        ToolsLogger.d("Battery tool finished: action=$action timeMs=$elapsed payload=$payload")
        return result
    }
}
