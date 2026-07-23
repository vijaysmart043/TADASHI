package com.vijay.tadashi.core.tools.media

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import com.vijay.tadashi.core.tools.common.ControllerResult
import com.vijay.tadashi.core.tools.notifications.TadashiNotificationListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMediaControlController @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaControlController {
    override fun sendMediaAction(action: MediaAction): ControllerResult<Unit> {
        val keyCode = when (action) {
            MediaAction.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
            MediaAction.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
            MediaAction.RESUME -> KeyEvent.KEYCODE_MEDIA_PLAY
            MediaAction.STOP -> KeyEvent.KEYCODE_MEDIA_STOP
            MediaAction.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
            MediaAction.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val now = SystemClock.uptimeMillis()
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
        return ControllerResult.Success(Unit, permissionStatus = "N/A")
    }

    override fun getCurrentStatus(): ControllerResult<MediaStatus> {
        val accessEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        if (!accessEnabled) {
            return ControllerResult.Success(
                value = MediaStatus(isPlaying = null, title = null, artist = null),
                permissionStatus = "NOTIFICATION_ACCESS_DENIED"
            )
        }

        val mgr = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val component = ComponentName(context, TadashiNotificationListenerService::class.java)
        val controller = runCatching { mgr.getActiveSessions(component).firstOrNull() }.getOrNull()
            ?: return ControllerResult.Success(
                value = MediaStatus(isPlaying = null, title = null, artist = null),
                permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
            )

        val status = controller.toMediaStatus()
        return ControllerResult.Success(
            value = status,
            permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
        )
    }

    private fun MediaController.toMediaStatus(): MediaStatus {
        val state = playbackState
        val isPlaying = state?.state?.let { it == PlaybackState.STATE_PLAYING }
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        return MediaStatus(
            isPlaying = isPlaying,
            title = title,
            artist = artist
        )
    }
}
