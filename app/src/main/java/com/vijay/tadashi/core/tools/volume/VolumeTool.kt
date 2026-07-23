package com.vijay.tadashi.core.tools.volume

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeTool @Inject constructor(
    private val controller: VolumeController
) : Tool {
    override val id: String = "VOLUME"
    override val displayName: String = "Volume"
    override val description: String =
        "Controls media volume. Actions: SET, INCREASE, DECREASE, MUTE. Supports percent values like 80."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.MEDIA

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase()
            ?: return ToolResult.Failure("Missing argument: action")

        val value = request.arguments["value"]?.trim()
        val unit = request.arguments["unit"]?.trim()?.uppercase()

        ToolsLogger.d("Volume tool: action=$action value=$value unit=$unit")

        val result = when (action) {
            "SET" -> setVolume(value)
            "INCREASE" -> increaseVolume(value)
            "DECREASE" -> decreaseVolume(value)
            "MUTE" -> mute()
            else -> ToolResult.Failure("Unsupported action: $action")
        }

        val elapsed = System.currentTimeMillis() - startMs
        ToolsLogger.d("Volume tool finished: action=$action timeMs=$elapsed result=${result::class.simpleName}")
        return result
    }

    private fun setVolume(valueRaw: String?): ToolResult {
        val target = valueRaw?.toIntOrNull()
            ?: return ToolResult.Failure("Missing or invalid value for volume")

        val applied = controller.setPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target.coerceIn(0, 100)) <= 3
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Volume set to $applied%")
    }

    private fun increaseVolume(deltaRaw: String?): ToolResult {
        val current = controller.getCurrentPercent()
            .getOrElse { return it.toFailure() }
        val delta = deltaRaw?.toIntOrNull() ?: 10
        val target = (current + delta).coerceIn(0, 100)

        val applied = controller.setPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target) <= 3
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Volume increased to $applied%")
    }

    private fun decreaseVolume(deltaRaw: String?): ToolResult {
        val current = controller.getCurrentPercent()
            .getOrElse { return it.toFailure() }
        val delta = deltaRaw?.toIntOrNull() ?: 10
        val target = (current - delta).coerceIn(0, 100)

        val applied = controller.setPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target) <= 3
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Volume decreased to $applied%")
    }

    private fun mute(): ToolResult {
        val applied = controller.setMuted()
            .getOrElse { return it.toFailure() }

        val verified = applied == 0
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Volume muted")
    }

    private fun Throwable.toFailure(): ToolResult {
        ToolsLogger.d("Volume error", this)
        return ToolResult.Failure(message ?: "Volume operation failed")
    }
}

