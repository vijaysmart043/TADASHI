package com.vijay.tadashi.core.ai.gemini.network

import com.vijay.tadashi.core.ai.AIConfiguration
import com.vijay.tadashi.core.ai.AIProvider
import com.vijay.tadashi.core.ai.AIResult
import javax.inject.Inject

/**
 * Maps between domain configuration and Gemini request/response payloads.
 */
class GeminiMapper @Inject constructor() {
    /**
     * Builds the official Gemini "generateContent" request.
     */
    fun toRequest(
        input: String,
        configuration: AIConfiguration
    ): GeminiGenerateContentRequest {
        return GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = input)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = configuration.temperature,
                maxOutputTokens = configuration.maxTokens
            )
        )
    }

    /**
     * Extracts assistant text and token usage (when available).
     */
    fun toResult(
        response: GeminiGenerateContentResponse
    ): AIResult {
        val text = extractText(response)
        val tokensUsed = response.usageMetadata?.totalTokenCount

        return if (text.isNullOrBlank()) {
            AIResult(
                text = "",
                success = false,
                error = "Gemini returned an empty response",
                tokensUsed = tokensUsed,
                provider = AIProvider.GEMINI
            )
        } else {
            AIResult(
                text = text,
                success = true,
                tokensUsed = tokensUsed,
                provider = AIProvider.GEMINI
            )
        }
    }

    private fun extractText(response: GeminiGenerateContentResponse): String? {
        val firstCandidate = response.candidates.firstOrNull() ?: return null
        val content = firstCandidate.content ?: return null
        val firstPart = content.parts.firstOrNull() ?: return null
        return firstPart.text
    }
}
