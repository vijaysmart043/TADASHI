package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIToolPlannerTest {
    private val planner = AIToolPlanner()

    @Test
    fun openApp_openChrome_matchesOpenAppTool() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Open Chrome"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        assertEquals("OPEN_APP", tool.request.toolId)
        assertEquals("chrome", tool.request.arguments["app"])
    }

    @Test
    fun openApp_openInstagram_matchesOpenAppTool() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Open Instagram"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        assertEquals("OPEN_APP", tool.request.toolId)
        assertEquals("instagram", tool.request.arguments["app"])
    }

    @Test
    fun flashlight_torchOn_matchesFlashlightOn() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Torch on"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        assertEquals("FLASHLIGHT", tool.request.toolId)
        assertEquals("ON", tool.request.arguments["action"])
    }

    @Test
    fun flashlight_turnOffFlashlight_matchesFlashlightOff() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Turn off flashlight"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        assertEquals("FLASHLIGHT", tool.request.toolId)
        assertEquals("OFF", tool.request.arguments["action"])
    }

    @Test
    fun chat_whatIsPython_continuesToGemini() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "What is Python?"
        )

        assertTrue(result.decision is PlannerDecision.ContinueToGemini)
    }
}

