package com.vijay.tadashi.core.tools.brightness

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrightnessTool @Inject constructor(
    private val controller: BrightnessController
) : Tool {
    override val id: String = "BRIGHTNESS"
    override val displayName: String = "Brightness"
    override val description: String =
        "Controls screen brightness. Actions: SET, INCREASE, DECREASE. Supports percent values like 40%."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase()
            ?: return ToolResult.Failure("Missing argument: action")

        val value = request.arguments["value"]?.trim()
        val unit = request.arguments["unit"]?.trim()?.uppercase()

        ToolsLogger.d("Brightness tool: action=$action value=$value unit=$unit")

        val result = when (action) {
            "SET" -> setBrightness(value)
            "INCREASE" -> increaseBrightness(value)
            "DECREASE" -> decreaseBrightness(value)
            else -> ToolResult.Failure("Unsupported action: $action")
        }

        val elapsed = System.currentTimeMillis() - startMs
        ToolsLogger.d("Brightness tool finished: action=$action timeMs=$elapsed result=${result::class.simpleName}")
        return result
    }

    private fun setBrightness(valueRaw: String?): ToolResult {
        val target = valueRaw?.toIntOrNull()
            ?: return ToolResult.Failure("Missing or invalid value for brightness")

        if (!controller.canWriteSettings()) {
            return ToolResult.Failure("Permission required: modify system settings")
        }

        val applied = controller.setBrightnessPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target.coerceIn(0, 100)) <= 2
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Brightness set to $applied%")
    }

    private fun increaseBrightness(deltaRaw: String?): ToolResult {
        if (!controller.canWriteSettings()) {
            return ToolResult.Failure("Permission required: modify system settings")
        }

        val current = controller.getBrightnessPercent()
            .getOrElse { return it.toFailure() }

        val delta = deltaRaw?.toIntOrNull() ?: 10
        val target = (current + delta).coerceIn(0, 100)
        val applied = controller.setBrightnessPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target) <= 2
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Brightness increased to $applied%")
    }

    private fun decreaseBrightness(deltaRaw: String?): ToolResult {
        if (!controller.canWriteSettings()) {
            return ToolResult.Failure("Permission required: modify system settings")
        }

        val current = controller.getBrightnessPercent()
            .getOrElse { return it.toFailure() }

        val delta = deltaRaw?.toIntOrNull() ?: 10
        val target = (current - delta).coerceIn(0, 100)
        val applied = controller.setBrightnessPercent(target)
            .getOrElse { return it.toFailure() }

        val verified = kotlin.math.abs(applied - target) <= 2
        if (!verified) {
            return ToolResult.Failure("Operation failed (verification failed)")
        }

        return ToolResult.Success("Brightness decreased to $applied%")
    }

    private fun Throwable.toFailure(): ToolResult {
        ToolsLogger.d("Brightness error", this)
        return when (this) {
            is SecurityException -> ToolResult.Failure("Permission required: modify system settings")
            else -> ToolResult.Failure(message ?: "Brightness operation failed")
        }
    }
}

