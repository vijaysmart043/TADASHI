package com.vijay.tadashi.core.ai.repository

import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import com.vijay.tadashi.core.ai.gemini.GeminiHttpClient
import com.vijay.tadashi.core.ai.gemini.GeminiRequestBuilder
import com.vijay.tadashi.core.ai.gemini.GeminiResponseParser
import javax.inject.Inject

/**
 * Default implementation of [AIRepository].
 *
 * Phase 3.1 provides only the wiring and request/response scaffolding; real networking will be added
 * in the next phase.
 */
class AIRepositoryImpl @Inject constructor(
    private val geminiHttpClient: GeminiHttpClient,
    private val geminiRequestBuilder: GeminiRequestBuilder,
    private val geminiResponseParser: GeminiResponseParser
) : AIRepository {

    override suspend fun generateResponse(
        provider: AIProvider,
        input: String,
        configuration: AIConfiguration
    ): AIResult {
        return when (provider) {
            AIProvider.GEMINI -> generateGeminiResponse(
                input = input,
                configuration = configuration
            )

            AIProvider.OPENAI,
            AIProvider.OLLAMA,
            AIProvider.RULE_BASED -> AIResult(
                text = "",
                success = false,
                error = "Provider not available in repository: $provider",
                provider = provider
            )
        }
    }

    private suspend fun generateGeminiResponse(
        input: String,
        configuration: AIConfiguration
    ): AIResult {
        val requestBody = geminiRequestBuilder.buildGenerateContentRequest(
            input = input,
            configuration = configuration
        )

        return AIResult(
            text = "Gemini Integration Ready",
            success = true,
            provider = AIProvider.GEMINI
        )
    }
}

