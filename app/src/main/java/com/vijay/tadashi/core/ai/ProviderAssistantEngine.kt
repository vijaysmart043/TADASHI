package com.vijay.tadashi.core.ai

import android.util.Log
import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.ai.streaming.StreamingResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Single entry-point [AssistantEngine] that routes requests to the configured provider engine.
 *
 * This keeps the presentation layer stable while new providers are introduced.
 */
class ProviderAssistantEngine @Inject constructor(
    private val configurationStore: AIConfigurationStore,
    private val ruleBasedAssistantEngine: RuleBasedAssistantEngine,
    private val geminiAssistantEngine: GeminiAssistantEngine
) : AssistantEngine {

    override suspend fun generateResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): AIResult {
        val provider = configurationStore.getConfiguration().selectedProvider
        Log.d(TAG, "Selected provider: $provider")

        return when (provider) {
            AIProvider.RULE_BASED -> {
                Log.d(TAG, "Engine selected: RuleBasedAssistantEngine")
                ruleBasedAssistantEngine.generateResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.GEMINI -> {
                Log.d(TAG, "Engine selected: GeminiAssistantEngine")
                geminiAssistantEngine.generateResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.OPENAI,
            AIProvider.OLLAMA -> AIResult(
                text = "",
                success = false,
                error = "Provider not implemented: $provider",
                provider = provider
            )
        }
    }

    override fun streamResponse(
        history: ConversationHistory,
        latestUserMessage: String
    ): Flow<StreamingResponse> {
        val provider = configurationStore.getConfiguration().selectedProvider
        Log.d(TAG, "Selected provider: $provider")

        return when (provider) {
            AIProvider.RULE_BASED -> {
                Log.d(TAG, "Engine selected: RuleBasedAssistantEngine")
                ruleBasedAssistantEngine.streamResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.GEMINI -> {
                Log.d(TAG, "Engine selected: GeminiAssistantEngine")
                geminiAssistantEngine.streamResponse(
                    history = history,
                    latestUserMessage = latestUserMessage
                )
            }

            AIProvider.OPENAI,
            AIProvider.OLLAMA -> flowOf(
                StreamingResponse.Error("Provider not implemented: $provider")
            )
        }
    }

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
    }
}
