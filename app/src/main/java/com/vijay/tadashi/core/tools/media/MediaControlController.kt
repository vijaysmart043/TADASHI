package com.vijay.tadashi.core.tools.media

import com.vijay.tadashi.core.tools.common.ControllerResult

interface MediaControlController {
    fun sendMediaAction(action: MediaAction): ControllerResult<Unit>
    fun getCurrentStatus(): ControllerResult<MediaStatus>
}

enum class MediaAction {
    PLAY,
    PAUSE,
    RESUME,
    STOP,
    NEXT,
    PREVIOUS
}

