package com.vijay.tadashi.core.tools.media

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
class MediaControlTool @Inject constructor(
    private val controller: MediaControlController
) : Tool {
    override val id: String = "MEDIA_CONTROL"
    override val displayName: String = "Media Control"
    override val description: String =
        "Controls media playback. Actions: PLAY, PAUSE, RESUME, STOP, NEXT, PREVIOUS, STATUS. STATUS may return playing state/title/artist when available."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.MEDIA

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val actionRaw = request.arguments["action"]?.trim()?.uppercase() ?: "STATUS"
        val app = request.arguments["app"]?.trim()

        ToolsLogger.d("Media tool: action=$actionRaw app=$app args=${request.arguments}")

        val result = when (actionRaw) {
            "PLAY" -> send(MediaAction.PLAY, startMs)
            "PAUSE" -> send(MediaAction.PAUSE, startMs)
            "RESUME" -> send(MediaAction.RESUME, startMs)
            "STOP" -> send(MediaAction.STOP, startMs)
            "NEXT" -> send(MediaAction.NEXT, startMs)
            "PREVIOUS" -> send(MediaAction.PREVIOUS, startMs)
            "STATUS", "STATE", "CURRENT" -> status(startMs)
            "TITLE" -> status(startMs, titleOnly = true)
            "ARTIST" -> status(startMs, artistOnly = true)
            else -> ToolExecutionReports.failure(
                message = "Unsupported action: $actionRaw",
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
            "Media tool finished: action=$actionRaw timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
        )
        return result
    }

    private fun send(action: MediaAction, startMs: Long): ToolResult {
        return when (val r = controller.sendMediaAction(action)) {
            is ControllerResult.Success -> ToolExecutionReports.success(
                message = when (action) {
                    MediaAction.PLAY -> "Play"
                    MediaAction.PAUSE -> "Paused"
                    MediaAction.RESUME -> "Resumed"
                    MediaAction.STOP -> "Stopped"
                    MediaAction.NEXT -> "Next track"
                    MediaAction.PREVIOUS -> "Previous track"
                },
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
                error = r.error ?: "MEDIA_ACTION_FAILED"
            )
        }
    }

    private fun status(
        startMs: Long,
        titleOnly: Boolean = false,
        artistOnly: Boolean = false
    ): ToolResult {
        return when (val r = controller.getCurrentStatus()) {
            is ControllerResult.Success -> {
                val s = r.value
                val message = when {
                    titleOnly -> s.title ?: "Title unavailable"
                    artistOnly -> s.artist ?: "Artist unavailable"
                    else -> buildString {
                        val playing = s.isPlaying
                        append(
                            when (playing) {
                                true -> "Playing"
                                false -> "Paused"
                                null -> "Playback state unavailable"
                            }
                        )
                        s.title?.let { append("\nTitle: $it") }
                        s.artist?.let { append("\nArtist: $it") }
                    }
                }
                val verifiable = s.isPlaying != null || s.title != null || s.artist != null
                ToolExecutionReports.success(
                    message = message,
                    verification = verifiable,
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
                error = r.error ?: "STATUS_FAILED"
            )
        }
    }
}
