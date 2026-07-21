package com.vijay.tadashi.presentation.voice

sealed interface VoiceEvents {
    data object RequestMicrophonePermission : VoiceEvents
    data class ShowToast(val message: String) : VoiceEvents
    data class NavigateToSettings(val message: String) : VoiceEvents
}
