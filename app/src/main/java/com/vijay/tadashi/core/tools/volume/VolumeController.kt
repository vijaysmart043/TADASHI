package com.vijay.tadashi.core.tools.volume

import android.content.Context
import android.media.AudioManager
import com.vijay.tadashi.core.tools.ToolsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class VolumeController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getCurrentPercent(): Result<Int> {
        return runCatching {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            levelToPercent(current, max)
        }.onFailure { e ->
            ToolsLogger.d("Volume error: read failed", e)
        }
    }

    fun setPercent(targetPercent: Int): Result<Int> {
        return runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetLevel = percentToLevel(targetPercent, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetLevel, 0)
            getCurrentPercent().getOrThrow()
        }.onFailure { e ->
            ToolsLogger.d("Volume error: set failed", e)
        }
    }

    fun setMuted(): Result<Int> {
        return runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            getCurrentPercent().getOrThrow()
        }.onFailure { e ->
            ToolsLogger.d("Volume error: mute failed", e)
        }
    }

    private fun percentToLevel(percent: Int, max: Int): Int {
        if (max <= 0) return 0
        return ((percent.coerceIn(0, 100) / 100.0) * max.toDouble())
            .roundToInt()
            .coerceIn(0, max)
    }

    private fun levelToPercent(level: Int, max: Int): Int {
        if (max <= 0) return 0
        return ((level.coerceIn(0, max) / max.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }
}

