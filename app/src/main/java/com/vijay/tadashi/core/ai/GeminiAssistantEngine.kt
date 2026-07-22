package com.vijay.tadashi.core.ai

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.repository.AIRepository
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Gemini-backed assistant engine.
 */
class GeminiAssistantEngine @Inject constructor(
    private val configurationStore: AIConfigurationStore,
    private val aiRepository: AIRepository
) : AssistantEngine {

    override suspend fun generateResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): AIResult {
        val configuration = configurationStore.getConfiguration()

        return aiRepository.generateResponse(
            provider = AIProvider.GEMINI,
            history = history,
            latestUserMessage = latestUserMessage,
            configuration = configuration
        )
    }

    override fun streamResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): Flow<StreamingResponse> {
        val configuration = configurationStore.getConfiguration()

        return aiRepository.streamResponse(
            provider = AIProvider.GEMINI,
            history = history,
            latestUserMessage = latestUserMessage,
            configuration = configuration
        )
    }
}
