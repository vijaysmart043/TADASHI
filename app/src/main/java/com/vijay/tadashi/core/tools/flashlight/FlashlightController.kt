package com.vijay.tadashi.core.tools.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import com.vijay.tadashi.core.tools.ToolsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashlightController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var torchEnabled: Boolean = false

    private val cameraIdWithFlash: String? = runCatching { findCameraIdWithFlash(cameraManager) }
        .onFailure { ToolsLogger.d("Flashlight error: failed to resolve cameraId", it) }
        .getOrNull()

    init {
        ToolsLogger.d("Flashlight available: ${isAvailable()}")

        if (cameraIdWithFlash != null) {
            runCatching {
                cameraManager.registerTorchCallback(torchCallback, handler)
            }.onFailure { e ->
                ToolsLogger.d("Flashlight error: failed to register torch callback", e)
            }
        }
    }

    fun isAvailable(): Boolean = cameraIdWithFlash != null

    fun isEnabled(): Result<Boolean> {
        if (!isAvailable()) return Result.failure(IllegalStateException("No flashlight available"))
        return Result.success(torchEnabled)
    }

    fun turnOn(): Result<Unit> = setTorch(enabled = true)

    fun turnOff(): Result<Unit> = setTorch(enabled = false)

    fun toggle(): Result<FlashlightState> {
        if (!isAvailable()) return Result.success(FlashlightState.Unavailable)

        val currentlyEnabled = isEnabled().getOrElse { false }
        val setResult = setTorch(enabled = !currentlyEnabled)
        if (setResult.isFailure) {
            return Result.failure(setResult.exceptionOrNull() ?: RuntimeException("Toggle failed"))
        }

        return Result.success(if (!currentlyEnabled) FlashlightState.On else FlashlightState.Off)
    }

    fun status(): Result<FlashlightState> {
        if (!isAvailable()) return Result.success(FlashlightState.Unavailable)
        return isEnabled().map { enabled ->
            if (enabled) FlashlightState.On else FlashlightState.Off
        }
    }

    private fun setTorch(enabled: Boolean): Result<Unit> {
        val cameraId = cameraIdWithFlash
            ?: return Result.failure(IllegalStateException("No flashlight available"))

        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            torchEnabled = enabled
            if (enabled) {
                ToolsLogger.d("Flashlight enabled")
            } else {
                ToolsLogger.d("Flashlight disabled")
            }
            Result.success(Unit)
        } catch (e: SecurityException) {
            ToolsLogger.d("Flashlight error: permission denied", e)
            Result.failure(e)
        } catch (e: CameraAccessException) {
            ToolsLogger.d("Flashlight error: camera unavailable", e)
            Result.failure(e)
        } catch (e: Exception) {
            ToolsLogger.d("Flashlight error: unexpected exception", e)
            Result.failure(e)
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == cameraIdWithFlash) {
                torchEnabled = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == cameraIdWithFlash) {
                ToolsLogger.d("Flashlight error: torch mode unavailable")
            }
        }
    }

    private fun findCameraIdWithFlash(cameraManager: CameraManager): String? {
        val cameraIds = cameraManager.cameraIdList ?: return null
        var fallback: String? = null

        for (id in cameraIds) {
            val characteristics = runCatching { cameraManager.getCameraCharacteristics(id) }.getOrNull()
                ?: continue

            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            if (!hasFlash) continue

            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) return id
            if (fallback == null) fallback = id
        }

        return fallback
    }
}
