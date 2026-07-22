package com.vijay.tadashi.core.ai

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedAssistantEngineTest {
    @Test
    fun generateResponse_returnsSuccess() = runBlocking {
        val engine = RuleBasedAssistantEngine()
        val result = engine.generateResponse(
            history = ConversationHistory(),
            latestUserMessage = "hello"
        )
        assertTrue(result.success)
        assertEquals(AIProvider.RULE_BASED, result.provider)
        assertTrue(result.text.isNotBlank())
    }
}
