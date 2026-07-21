package com.vijay.tadashi.core.ai.gemini.network

import com.vijay.tadashi.core.ai.AIConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiMapperTest {
    private val mapper = GeminiMapper()

    @Test
    fun toRequest_includesUserText() {
        val request = mapper.toRequest(
            input = "Hello",
            configuration = AIConfiguration(modelName = "gemini-2.5-flash")
        )

        assertEquals("Hello", request.contents.first().parts.first().text)
    }

    @Test
    fun toResult_extractsTextAndTokens() {
        val response = GeminiGenerateContentResponse(
            candidates = listOf(
                GeminiCandidate(
                    content = GeminiContent(
                        parts = listOf(GeminiPart(text = "Hi from Gemini"))
                    )
                )
            ),
            usageMetadata = GeminiUsageMetadata(totalTokenCount = 42)
        )

        val result = mapper.toResult(response)
        assertTrue(result.success)
        assertEquals("Hi from Gemini", result.text)
        assertEquals(42, result.tokensUsed)
    }

    @Test
    fun toResult_emptyResponseIsFailure() {
        val result = mapper.toResult(GeminiGenerateContentResponse())
        assertFalse(result.success)
    }
}
