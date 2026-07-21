package com.vijay.tadashi.core.ai.conversation

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationManager @Inject constructor() {
    private val _history = MutableStateFlow(ConversationHistory())
    val history: StateFlow<ConversationHistory> = _history.asStateFlow()

    val systemPrompt: String = SYSTEM_PROMPT

    fun addUserMessage(text: String): ConversationMessage {
        return addMessage(
            ConversationMessage(
                role = ConversationRole.USER,
                text = text
            )
        )
    }

    fun addAssistantMessage(text: String): ConversationMessage {
        return addMessage(
            ConversationMessage(
                role = ConversationRole.ASSISTANT,
                text = text
            )
        )
    }

    fun clearHistory() {
        _history.value = ConversationHistory()
        Log.d(TAG, "History cleared")
    }

    fun getHistory(): ConversationHistory = _history.value

    private fun addMessage(message: ConversationMessage): ConversationMessage {
        val current = _history.value.messages
        val updated = (current + message)
            .sortedBy { it.timestamp }
            .takeLast(MAX_HISTORY_SIZE)

        _history.value = ConversationHistory(messages = updated)
        Log.d(TAG, "Message added (role=${message.role}, size=${updated.size})")
        return message
    }

    companion object {
        private const val TAG = "TADASHI-CONTEXT"
        private const val MAX_HISTORY_SIZE = 20

        const val SYSTEM_PROMPT = "You are TADASHI, an intelligent Android AI assistant.\n\nAlways answer as TADASHI.\n\nMaintain conversation context.\n\nWhen user requests code, always return complete executable code inside Markdown code blocks.\n\nBe concise unless the user requests detailed explanations."
    }
}
