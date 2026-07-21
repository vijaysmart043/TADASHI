package com.vijay.tadashi.core.ai

import android.util.Log
import com.vijay.tadashi.core.ai.conversation.ConversationHistory
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

    private companion object {
        private const val TAG = "TADASHI-GEMINI"
    }
}
