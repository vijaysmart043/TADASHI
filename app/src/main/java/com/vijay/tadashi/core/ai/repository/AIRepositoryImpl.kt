package com.vijay.tadashi.core.ai.repository

import android.util.Log
import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import com.vijay.tadashi.core.ai.gemini.network.GeminiMapper
import com.vijay.tadashi.core.ai.gemini.network.GeminiService
import com.vijay.tadashi.core.ai.gemini.network.GeminiServiceResult
import javax.inject.Inject

/**
 * Default implementation of [AIRepository].
 */
class AIRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val geminiMapper: GeminiMapper
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
        return when (
            val result = geminiService.generateContent(
                input = input,
                configuration = configuration
            )
        ) {
            is GeminiServiceResult.Success -> {
                val aiResult = geminiMapper.toResult(result.response)
                Log.d(TAG, "Tokens used: ${aiResult.tokensUsed ?: -1}")
                aiResult
            }

            is GeminiServiceResult.Error -> AIResult(
                text = "",
                success = false,
                error = result.message,
                provider = AIProvider.GEMINI
            )
        }
    }

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
    }
}
