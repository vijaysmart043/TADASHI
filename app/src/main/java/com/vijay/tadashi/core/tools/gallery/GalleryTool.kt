package com.vijay.tadashi.core.tools.gallery

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
class GalleryTool @Inject constructor(
    private val controller: GalleryController
) : Tool {
    override val id: String = "GALLERY"
    override val displayName: String = "Gallery"
    override val description: String =
        "Opens gallery and can open/share latest image. Actions: OPEN, OPEN_LATEST, SHARE_LATEST."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.OTHER

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "OPEN"

        ToolsLogger.d("Gallery tool: action=$action args=${request.arguments}")

        val result = when (action) {
            "OPEN" -> exec(startMs, "Opening gallery") { controller.openGallery() }
            "OPEN_LATEST", "LATEST" -> exec(startMs, "Opening latest image") { controller.openLatestImage() }
            "SHARE_LATEST", "SHARE" -> exec(startMs, "Sharing latest image") { controller.shareLatestImage() }
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
            "Gallery tool finished: action=$action timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
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
                error = r.error ?: "GALLERY_FAILED"
            )
        }
    }
}
