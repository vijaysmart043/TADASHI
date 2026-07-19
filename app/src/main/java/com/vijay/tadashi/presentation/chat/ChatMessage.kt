package com.vijay.tadashi.presentation.chat

import java.util.UUID

enum class Sender {
    USER, ASSISTANT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)
