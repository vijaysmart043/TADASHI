package com.vijay.tadashi.core.ai

import com.vijay.tadashi.core.ai.repository.AIRepository
import javax.inject.Inject

/**
 * Gemini-backed assistant engine.
 *
 * Phase 3.1 intentionally does not execute real networking. The repository layer and Gemini request
 * scaffolding are injected so that a future phase can enable remote calls without impacting
 * [com.vijay.tadashi.presentation.voice.VoiceViewModel].
 */
class GeminiAssistantEngine @Inject constructor(
    private val configurationStore: AIConfigurationStore,
    private val aiRepository: AIRepository
) : AssistantEngine {

    override suspend fun generateResponse(input: String): AIResult {
        val configuration = configurationStore.getConfiguration()

        return aiRepository.generateResponse(
            provider = AIProvider.GEMINI,
            input = input,
            configuration = configuration
        )
    }
}

