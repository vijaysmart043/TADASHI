package com.vijay.tadashi.core.tools.flashlight

sealed interface FlashlightState {
    data object On : FlashlightState
    data object Off : FlashlightState
    data object Unavailable : FlashlightState
}
