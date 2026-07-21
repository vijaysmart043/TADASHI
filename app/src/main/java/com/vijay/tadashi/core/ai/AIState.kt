package com.vijay.tadashi.core.ai

/**
 * High-level UI state for AI response generation.
 */
sealed class AIState {
    /**
     * No in-flight request.
     */
    data object Idle : AIState()

    /**
     * Request is currently being processed.
     */
    data object Loading : AIState()

    /**
     * Request succeeded.
     */
    data class Success(
        val result: AIResult
    ) : AIState()

    /**
     * Request failed.
     */
    data class Error(
        val message: String,
        val provider: AIProvider? = null
    ) : AIState()
}

