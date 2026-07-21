package com.vijay.tadashi.core.ai.gemini.network

import com.vijay.tadashi.core.ai.AIConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
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

        val firstPart = request.json["contents"]!!.jsonArray[0]
            .jsonObject["parts"]!!.jsonArray[0]
            .jsonObject

        assertEquals("Hello", firstPart["text"]!!.jsonPrimitive.content)
    }

    @Test
    fun toResult_extractsTextAndTokens() {
        val responseJson: JsonObject = buildJsonObject {
            put(
                "candidates",
                kotlinx.serialization.json.buildJsonArray {
                    add(
                        buildJsonObject {
                            put(
                                "content",
                                buildJsonObject {
                                    put(
                                        "parts",
                                        kotlinx.serialization.json.buildJsonArray {
                                            add(buildJsonObject { put("text", JsonPrimitive("Hi from Gemini")) })
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
            put(
                "usageMetadata",
                buildJsonObject {
                    put("totalTokenCount", JsonPrimitive(42))
                }
            )
        }

        val result = mapper.toResult(GeminiResponse(json = responseJson))
        assertTrue(result.success)
        assertEquals("Hi from Gemini", result.text)
        assertEquals(42, result.tokensUsed)
    }

    @Test
    fun toResult_emptyResponseIsFailure() {
        val responseJson: JsonObject = buildJsonObject { }
        val result = mapper.toResult(GeminiResponse(json = responseJson))
        assertFalse(result.success)
    }
}
