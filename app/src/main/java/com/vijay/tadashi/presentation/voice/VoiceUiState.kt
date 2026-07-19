package com.vijay.tadashi.presentation.voice

import com.vijay.tadashi.presentation.chat.ChatMessage

data class VoiceUiState(
    val chatHistory: List<ChatMessage> = emptyList(),
    val isListening: Boolean = false,
    val hasPermission: Boolean = false,
    val userInput: String = ""
)
