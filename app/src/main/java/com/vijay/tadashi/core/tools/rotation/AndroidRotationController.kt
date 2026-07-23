package com.vijay.tadashi.core.tools.rotation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidRotationController @Inject constructor(
    @ApplicationContext private val context: Context
) : RotationController {
    override fun status(): RotationStatusResult {
        return runCatching {
            val enabled = Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                0
            ) == 1
            RotationStatusResult(
                enabled = enabled,
                message = if (enabled) "Auto-rotate is ON" else "Auto-rotate is OFF",
                error = null
            )
        }.getOrElse { e ->
            RotationStatusResult(
                enabled = null,
                message = "Failed to read auto-rotate status",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    override fun setAutoRotateEnabled(enabled: Boolean): RotationChangeResult {
        if (!Settings.System.canWrite(context)) {
            openWriteSettings()
            return RotationChangeResult(
                verification = false,
                enabled = status().enabled,
                requiresUserAction = true,
                message = "Permission required to change auto-rotate. Please allow Modify system settings for TADASHI.",
                error = "WRITE_SETTINGS"
            )
        }

        return runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0
            )
            val current = status().enabled
            RotationChangeResult(
                verification = current == enabled,
                enabled = current,
                requiresUserAction = false,
                message = if (current == enabled) {
                    if (enabled) "Auto-rotate enabled" else "Auto-rotate disabled"
                } else {
                    "Auto-rotate change request did not apply"
                },
                error = null
            )
        }.getOrElse { e ->
            RotationChangeResult(
                verification = false,
                enabled = status().enabled,
                requiresUserAction = false,
                message = "Auto-rotate operation failed",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    private fun openWriteSettings() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

