package com.vijay.tadashi.core.ai

import javax.inject.Inject

/**
 * Deterministic, offline assistant engine used as the default provider.
 */
class RuleBasedAssistantEngine @Inject constructor() : AssistantEngine {
    override suspend fun generateResponse(input: String): AIResult {
        val text = when {
            input.contains("hello", ignoreCase = true) -> "Hello! How can I help you today?"
            input.contains("how are you", ignoreCase = true) -> "I'm just a bot, but I'm here to help!"
            input.contains("bye", ignoreCase = true) -> "Goodbye! Have a great day!"
            else -> "I'm sorry, I don't understand. Can you rephrase?"
        }

        return AIResult(
            text = text,
            success = true,
            provider = AIProvider.RULE_BASED
        )
    }
}
