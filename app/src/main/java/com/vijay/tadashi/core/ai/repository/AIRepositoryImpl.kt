package com.vijay.tadashi.core.ai.repository

import android.util.Log
import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import com.vijay.tadashi.core.ai.conversation.ContextBuilder
import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.gemini.network.GeminiMapper
import com.vijay.tadashi.core.ai.gemini.network.GeminiService
import com.vijay.tadashi.core.ai.gemini.network.GeminiServiceResult
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Default implementation of [AIRepository].
 */
class AIRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val geminiMapper: GeminiMapper,
    private val contextBuilder: ContextBuilder
) : AIRepository {

    override suspend fun generateResponse(
        provider: AIProvider,
        history: ConversationHistory,
        latestUserMessage: String,
        configuration: AIConfiguration
    ): AIResult {
        return when (provider) {
            AIProvider.GEMINI -> generateGeminiResponse(
                history = history,
                latestUserMessage = latestUserMessage,
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

    override fun streamResponse(
        provider: AIProvider,
        history: ConversationHistory,
        latestUserMessage: String,
        configuration: AIConfiguration
    ): Flow<StreamingResponse> {
        return when (provider) {
            AIProvider.GEMINI -> {
                val request = contextBuilder.buildGeminiRequest(
                    history = history,
                    latestUserMessage = latestUserMessage,
                    configuration = configuration
                )

                geminiService.generateContentStream(
                    request = request,
                    configuration = configuration
                )
            }

            AIProvider.OPENAI,
            AIProvider.OLLAMA,
            AIProvider.RULE_BASED -> flowOf(
                StreamingResponse.Error("Provider not available in repository: $provider")
            )
        }
    }

    private suspend fun generateGeminiResponse(
        history: ConversationHistory,
        latestUserMessage: String,
        configuration: AIConfiguration
    ): AIResult {
        val request = contextBuilder.buildGeminiRequest(
            history = history,
            latestUserMessage = latestUserMessage,
            configuration = configuration
        )

        return when (
            val result = geminiService.generateContent(
                request = request,
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
