package com.vijay.tadashi.core.tools

import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutor @Inject constructor(
    private val registry: ToolRegistry,
    private val permissionManager: ToolPermissionManager
) {
    suspend fun execute(request: ToolRequest): ToolResult {
        val tool = registry.find(request.toolId)
            ?: return ToolResult.Unsupported("Tool not registered: ${request.toolId}")

        val permissionStatus = permissionManager.checkPermissions(tool.requiredPermissions)
        if (permissionStatus != ToolPermissionStatus.Granted) {
            ToolsLogger.d("Execution result (toolId=${tool.id}): PermissionDenied")
            return ToolResult.PermissionDenied(permissionStatus)
        }

        ToolsLogger.d("Tool executed: ${tool.id}")
        return try {
            val result = tool.execute(request)
            ToolsLogger.d("Execution result (toolId=${tool.id}): ${result::class.simpleName}")
            result
        } catch (e: CancellationException) {
            ToolsLogger.d("Execution result (toolId=${tool.id}): Cancelled")
            ToolResult.Cancelled("Cancelled")
        } catch (e: Exception) {
            ToolsLogger.d("Execution result (toolId=${tool.id}): Failure", e)
            ToolResult.Failure("Tool execution failed")
        }
    }
}
