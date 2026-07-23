package com.vijay.tadashi.core.tools.deviceinfo

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
class DeviceInfoTool @Inject constructor(
    private val provider: DeviceInfoProvider
) : Tool {
    override val id: String = "DEVICE_INFO"
    override val displayName: String = "Device Information"
    override val description: String =
        "Returns device model, manufacturer, Android version, API level, RAM, storage, CPU ABI, and screen resolution."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        ToolsLogger.d("DeviceInfo tool: args=${request.arguments}")

        val result = run {
            val info = provider.getDeviceInfo().getOrElse { e ->
                return@run ToolExecutionReports.failure(
                    message = "Failed to read device information",
                    verification = false,
                    executionTimeMs = System.currentTimeMillis() - startMs,
                    error = e.message ?: e::class.java.simpleName
                )
            }

            val message = buildString {
                append("Model: ${info.model}")
                append(", Manufacturer: ${info.manufacturer}")
                append(", Android: ${info.androidVersion}")
                append(", API: ${info.apiLevel}")
                info.totalRamBytes?.let { append(", RAM: ${formatBytes(it)}") }
                info.totalStorageBytes?.let { append(", Storage: ${formatBytes(it)}") }
                if (info.cpuAbis.isNotEmpty()) append(", CPU ABI: ${info.cpuAbis.joinToString()}")
                info.screenResolution?.let { append(", Resolution: $it") }
            }

            ToolExecutionReports.success(
                message = message,
                verification = true,
                executionTimeMs = System.currentTimeMillis() - startMs
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
        ToolsLogger.d("DeviceInfo tool finished: timeMs=$elapsed payload=$payload")
        return result
    }

    private fun formatBytes(bytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        return when {
            bytes >= gb -> String.format("%.2f GB", bytes / gb)
            bytes >= mb -> String.format("%.2f MB", bytes / mb)
            bytes >= kb -> String.format("%.2f KB", bytes / kb)
            else -> "$bytes B"
        }
    }
}
