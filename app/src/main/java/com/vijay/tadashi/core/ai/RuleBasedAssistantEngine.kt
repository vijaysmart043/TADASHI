package com.vijay.tadashi.core.ai

class RuleBasedAssistantEngine : AssistantEngine {
    override fun generateResponse(input: String): String {
        return when {
            input.contains("hello", ignoreCase = true) -> "Hello! How can I help you today?"
            input.contains("how are you", ignoreCase = true) -> "I'm just a bot, but I'm here to help!"
            input.contains("bye", ignoreCase = true) -> "Goodbye! Have a great day!"
            else -> "I'm sorry, I don't understand. Can you rephrase?"
        }
    }
}
