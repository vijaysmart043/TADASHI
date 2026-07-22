package com.vijay.tadashi.core.ai

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Deterministic, offline assistant engine used as the default provider.
 */
class RuleBasedAssistantEngine @Inject constructor() : AssistantEngine {
    override suspend fun generateResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): AIResult {
        val text = when {
            latestUserMessage.contains("hello", ignoreCase = true) -> "Hello! How can I help you today?"
            latestUserMessage.contains("how are you", ignoreCase = true) -> "I'm just a bot, but I'm here to help!"
            latestUserMessage.contains("bye", ignoreCase = true) -> "Goodbye! Have a great day!"
            else -> "I'm sorry, I don't understand. Can you rephrase?"
        }

        return AIResult(
            text = text,
            success = true,
            provider = AIProvider.RULE_BASED
        )
    }

    override fun streamResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): Flow<StreamingResponse> {
        val text = when {
            latestUserMessage.contains("hello", ignoreCase = true) -> "Hello! How can I help you today?"
            latestUserMessage.contains("how are you", ignoreCase = true) -> "I'm just a bot, but I'm here to help!"
            latestUserMessage.contains("bye", ignoreCase = true) -> "Goodbye! Have a great day!"
            else -> "I'm sorry, I don't understand. Can you rephrase?"
        }

        return flowOf(
            StreamingResponse.Chunk(text),
            StreamingResponse.Completed(text)
        )
    }
}
