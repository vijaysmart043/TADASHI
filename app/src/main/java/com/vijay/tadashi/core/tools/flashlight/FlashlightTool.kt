package com.vijay.tadashi.core.tools.flashlight

import android.hardware.camera2.CameraAccessException
import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashlightTool @Inject constructor(
    private val controller: FlashlightController
) : Tool {
    override val id: String = "FLASHLIGHT"
    override val displayName: String = "Flashlight"
    override val description: String =
        "Controls the device flashlight. Actions: ON, OFF, TOGGLE, STATUS. Example commands: Turn on flashlight, Turn off flashlight, Toggle flashlight, Torch on, Torch off, Light on, Light off."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult {
        val rawAction = request.arguments["action"] ?: return ToolResult.Failure("Missing argument: action")
        val action = Action.from(rawAction) ?: return ToolResult.Failure("Unsupported action: $rawAction")

        ToolsLogger.d("Flashlight action: $action")

        return when (action) {
            Action.ON -> controller.turnOn().toToolResult(
                successMessage = "Flashlight turned on"
            )

            Action.OFF -> controller.turnOff().toToolResult(
                successMessage = "Flashlight turned off"
            )

            Action.TOGGLE -> controller.toggle()
                .mapCatching { state ->
                    when (state) {
                        FlashlightState.On -> ToolResult.Success("Flashlight turned on")
                        FlashlightState.Off -> ToolResult.Success("Flashlight turned off")
                        FlashlightState.Unavailable -> ToolResult.Failure("No flashlight available")
                    }
                }
                .getOrElse { it.toFailureResult() }

            Action.STATUS -> controller.status()
                .mapCatching { state ->
                    when (state) {
                        FlashlightState.On -> ToolResult.Success("Flashlight is ON")
                        FlashlightState.Off -> ToolResult.Success("Flashlight is OFF")
                        FlashlightState.Unavailable -> ToolResult.Success("Flashlight is unavailable")
                    }
                }
                .getOrElse { it.toFailureResult() }
        }
    }

    private enum class Action {
        ON,
        OFF,
        TOGGLE,
        STATUS;

        companion object {
            fun from(raw: String): Action? = when (raw.trim().uppercase()) {
                "ON" -> ON
                "OFF" -> OFF
                "TOGGLE" -> TOGGLE
                "STATUS" -> STATUS
                else -> null
            }
        }
    }

    private fun Result<Unit>.toToolResult(successMessage: String): ToolResult {
        return fold(
            onSuccess = { ToolResult.Success(successMessage) },
            onFailure = { it.toFailureResult() }
        )
    }

    private fun Throwable.toFailureResult(): ToolResult {
        return when (this) {
            is SecurityException -> ToolResult.Failure("Permission denied for flashlight")
            is CameraAccessException -> ToolResult.Failure("Camera unavailable")
            is IllegalStateException -> ToolResult.Failure(message ?: "Flashlight unavailable")
            else -> ToolResult.Failure(message ?: "Flashlight action failed")
        }.also {
            ToolsLogger.d("Flashlight error: ${it.message}", this)
        }
    }
}
