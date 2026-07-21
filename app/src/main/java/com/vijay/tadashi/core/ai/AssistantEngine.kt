package com.vijay.tadashi.core.ai

/**
 * Abstraction used by the presentation layer to generate an assistant response.
 *
 * This interface is intentionally provider-agnostic: callers must not know whether the response comes
 * from a rule-based implementation or a remote AI provider.
 */
interface AssistantEngine {
    /**
     * Generates an assistant response for the given input.
     */
    suspend fun generateResponse(
        input: String
    ): AIResult
}
