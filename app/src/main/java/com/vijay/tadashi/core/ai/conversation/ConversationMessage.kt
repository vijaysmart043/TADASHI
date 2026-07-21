package com.vijay.tadashi.core.ai.conversation

import java.util.UUID

data class ConversationMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ConversationRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

