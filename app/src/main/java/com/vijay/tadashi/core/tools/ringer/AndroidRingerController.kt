package com.vijay.tadashi.core.tools.ringer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidRingerController @Inject constructor(
    @ApplicationContext private val context: Context
) : RingerController {
    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    override fun status(): RingerStatusResult {
        val manager = audioManager
            ?: return RingerStatusResult(
                mode = null,
                message = "Audio service unavailable",
                error = "AUDIO_SERVICE unavailable"
            )

        return runCatching {
            val mode = when (manager.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
                AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
                else -> RingerMode.NORMAL
            }
            RingerStatusResult(
                mode = mode,
                message = "Ringer mode: ${mode.name}",
                error = null
            )
        }.getOrElse { e ->
            RingerStatusResult(
                mode = null,
                message = "Failed to read ringer mode",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    override fun setMode(mode: RingerMode): RingerChangeResult {
        val manager = audioManager
            ?: return RingerChangeResult(
                verification = false,
                mode = null,
                requiresUserAction = false,
                message = "Audio service unavailable",
                error = "AUDIO_SERVICE unavailable"
            )

        return runCatching {
            val target = when (mode) {
                RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
                RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
            }

            manager.ringerMode = target
            val current = status().mode
            val verified = current == mode

            if (!verified && mayNeedNotificationPolicyAccess()) {
                openNotificationPolicySettings()
                return@runCatching RingerChangeResult(
                    verification = false,
                    mode = current,
                    requiresUserAction = true,
                    message = "Unable to change ringer mode. Please allow Do Not Disturb access for TADASHI.",
                    error = "NOTIFICATION_POLICY_ACCESS"
                )
            }

            RingerChangeResult(
                verification = verified,
                mode = current,
                requiresUserAction = false,
                message = if (verified) {
                    "Ringer mode set to ${mode.name}"
                } else {
                    "Ringer mode change request did not apply"
                },
                error = null
            )
        }.getOrElse { e ->
            RingerChangeResult(
                verification = false,
                mode = status().mode,
                requiresUserAction = false,
                message = "Ringer mode operation failed",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    private fun mayNeedNotificationPolicyAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        return notificationManager?.isNotificationPolicyAccessGranted != true
    }

    private fun openNotificationPolicySettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

