package com.vijay.tadashi.core.ai

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

    override suspend fun generateResponse(input: String): AIResult {
        val provider = configurationStore.getConfiguration().selectedProvider

        return when (provider) {
            AIProvider.RULE_BASED -> ruleBasedAssistantEngine.generateResponse(input)
            AIProvider.GEMINI -> geminiAssistantEngine.generateResponse(input)
            AIProvider.OPENAI,
            AIProvider.OLLAMA -> AIResult(
                text = "",
                success = false,
                error = "Provider not implemented: $provider",
                provider = provider
            )
        }
    }
}

