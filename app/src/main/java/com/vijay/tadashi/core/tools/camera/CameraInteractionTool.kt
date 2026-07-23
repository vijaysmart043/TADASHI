package com.vijay.tadashi.core.tools.camera

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
class CameraInteractionTool @Inject constructor(
    private val controller: CameraController
) : Tool {
    override val id: String = "CAMERA_INTERACTION"
    override val displayName: String = "Camera"
    override val description: String =
        "Opens camera and can launch photo capture. Actions: OPEN, TAKE_PICTURE, SWITCH_CAMERA, VIDEO_MODE. Optional lens: FRONT/BACK."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.CAMERA

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "OPEN"
        val lens = request.arguments["lens"]?.trim()
            ?: request.arguments["camera"]?.trim()
            ?: request.arguments["facing"]?.trim()

        ToolsLogger.d("Camera tool: action=$action lens=$lens args=${request.arguments}")

        val result = when (action) {
            "OPEN" -> exec(startMs, "Opening camera") { controller.openCamera(lens) }
            "TAKE_PICTURE", "PHOTO", "SNAP" -> exec(startMs, "Launching photo capture") { controller.takePicture(lens) }
            "SWITCH_CAMERA", "SWITCH" -> exec(startMs, "Switching camera") { controller.switchCamera(lens) }
            "VIDEO_MODE", "VIDEO" -> exec(startMs, "Opening video mode") { controller.openVideoMode(lens) }
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
            "Camera tool finished: action=$action timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
        )
        return result
    }

    private fun exec(
        startMs: Long,
        message: String,
        block: () -> ControllerResult<Unit>
    ): ToolResult {
        return when (val r = block()) {
            is ControllerResult.Success -> ToolExecutionReports.success(
                message = message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs
            )
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
                error = r.error ?: "CAMERA_FAILED"
            )
        }
    }
}
