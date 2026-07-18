package com.vijay.tadashi.presentation.voice

data class VoiceUiState(
    val recognizedText: String = "",
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val hasPermission: Boolean = false
)
