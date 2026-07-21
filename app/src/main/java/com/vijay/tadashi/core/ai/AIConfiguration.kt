package com.vijay.tadashi.core.ai

/**
 * Persisted configuration used to select and parameterize the active AI provider.
 *
 * All secrets (e.g., [apiKey]) must be stored using encrypted persistence.
 */
data class AIConfiguration(
    val selectedProvider: AIProvider = AIProvider.RULE_BASED,
    val apiKey: String = "",
    val modelName: String = "",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512
)

