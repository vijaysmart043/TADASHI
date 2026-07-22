package com.vijay.tadashi.core.ai

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction used by the presentation layer to generate an assistant response.
 *
 * This interface is intentionally provider-agnostic: callers must not know whether the response comes
 * from a rule-based implementation or a remote AI provider.
 */
interface AssistantEngine {
    /**
     * Generates an assistant response using the prior conversation [history] and the
     * [latestUserMessage] as the next turn.
     */
    suspend fun generateResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): AIResult

    fun streamResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): Flow<StreamingResponse>
}
