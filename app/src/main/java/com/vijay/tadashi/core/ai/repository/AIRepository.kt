package com.vijay.tadashi.core.ai.repository

import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import com.vijay.tadashi.core.ai.conversation.ConversationHistory

/**
 * Data boundary for remote/local AI providers.
 *
 * Phase 3.1 keeps this interface intentionally small; future phases can extend this contract to
 * support streaming responses and richer metadata without affecting the presentation layer.
 */
interface AIRepository {
    /**
     * Generates a response using the requested [provider] and [configuration].
     */
    suspend fun generateResponse(
        provider: AIProvider,
        history: ConversationHistory,
        latestUserMessage: String,
        configuration: AIConfiguration
    ): AIResult
}
