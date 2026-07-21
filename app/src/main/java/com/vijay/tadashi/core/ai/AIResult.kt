package com.vijay.tadashi.core.ai

/**
 * Standard result returned by any [AssistantEngine] implementation.
 *
 * @property text The generated assistant text (empty if [success] is false).
 * @property success Whether the response generation succeeded.
 * @property error Optional error message for user display/logging.
 * @property tokensUsed Optional usage metadata for remote providers.
 * @property provider The provider that produced (or attempted) the response.
 */
data class AIResult(
    val text: String,
    val success: Boolean,
    val error: String? = null,
    val tokensUsed: Int? = null,
    val provider: AIProvider
)

