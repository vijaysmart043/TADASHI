package com.vijay.tadashi.core.ai.conversation

import android.util.Log
import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.gemini.network.GeminiContent
import com.vijay.tadashi.core.ai.gemini.network.GeminiGenerationConfig
import com.vijay.tadashi.core.ai.gemini.network.GeminiGenerateContentRequest
import com.vijay.tadashi.core.ai.gemini.network.GeminiPart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextBuilder @Inject constructor() {
    fun buildGeminiRequest(
        history: ConversationHistory,
        latestUserMessage: String,
        configuration: AIConfiguration
    ): GeminiGenerateContentRequest {
        val orderedHistory = history.messages.sortedBy { it.timestamp }

        val historyContents = orderedHistory.mapNotNull { message ->
            when (message.role) {
                ConversationRole.USER -> GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = message.text))
                )

                ConversationRole.ASSISTANT -> GeminiContent(
                    role = "model",
                    parts = listOf(GeminiPart(text = message.text))
                )

                ConversationRole.SYSTEM -> null
            }
        }

        val contents = historyContents + GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = latestUserMessage))
        )

        Log.d(
            TAG,
            "Context built (contextSize=${contents.size + 1}, messagesSent=${contents.size}, systemPromptIncluded=${ConversationManager.SYSTEM_PROMPT.isNotBlank()})"
        )
        val rolesSummary = contents.joinToString(separator = " | ") { it.role.orEmpty() }
        Log.d(TAG, "Messages sent roles: $rolesSummary")

        return GeminiGenerateContentRequest(
            systemInstruction = GeminiContent(
                role = "system",
                parts = listOf(GeminiPart(text = ConversationManager.SYSTEM_PROMPT))
            ),
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = configuration.temperature,
                maxOutputTokens = configuration.maxTokens
            )
        )
    }

    private companion object {
        private const val TAG = "TADASHI-CONTEXT"
    }
}
