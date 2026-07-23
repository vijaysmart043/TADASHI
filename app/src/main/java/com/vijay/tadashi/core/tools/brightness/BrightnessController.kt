package com.vijay.tadashi.core.tools.brightness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.vijay.tadashi.core.tools.ToolsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

interface BrightnessController {
    fun canWriteSettings(): Boolean
    fun requestWriteSettingsPermission()
    fun getBrightnessPercent(): Result<Int>
    fun setBrightnessPercent(targetPercent: Int): Result<Int>
}

@Singleton
class AndroidBrightnessController @Inject constructor(
    @ApplicationContext private val context: Context
) : BrightnessController {
    override fun canWriteSettings(): Boolean = Settings.System.canWrite(context)

    override fun requestWriteSettingsPermission() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getBrightnessPercent(): Result<Int> {
        return runCatching {
            val value = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            rawToPercent(value)
        }.onFailure { e ->
            ToolsLogger.d("Brightness error: read failed", e)
        }
    }

    override fun setBrightnessPercent(targetPercent: Int): Result<Int> {
        return runCatching {
            if (!canWriteSettings()) {
                throw SecurityException("WRITE_SETTINGS not granted")
            }

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )

            val clamped = targetPercent.coerceIn(0, 100)
            val raw = percentToRaw(clamped)
            val ok = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                raw
            )
            if (!ok) {
                throw IllegalStateException("Failed to write brightness")
            }

            getBrightnessPercent().getOrThrow()
        }.onFailure { e ->
            ToolsLogger.d("Brightness error: write failed", e)
        }
    }

    private fun percentToRaw(percent: Int): Int {
        return (percent.coerceIn(0, 100) / 100.0 * 255.0).roundToInt().coerceIn(0, 255)
    }

    private fun rawToPercent(raw: Int): Int {
        return (raw.coerceIn(0, 255) / 255.0 * 100.0).roundToInt().coerceIn(0, 100)
    }
}
