package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import kotlinx.serialization.json.Json
import com.vijay.tadashi.core.tools.ToolParser
import com.vijay.tadashi.core.ai.conversation.ConversationMessage
import com.vijay.tadashi.core.ai.conversation.ConversationRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIToolPlannerTest {
    private val planner = AIToolPlanner(
        classifier = IntentClassifier(),
        parameterExtractor = ParameterExtractor()
    )

    private val parser = ToolParser(
        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    )

    @Test
    fun multiAction_openChromeAndWhatsApp() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Open Chrome and WhatsApp"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(2, steps.size)
        assertEquals("OPEN_APP", steps[0].request.toolId)
        assertEquals("chrome", steps[0].request.arguments["app"])
        assertEquals("OPEN_APP", steps[1].request.toolId)
        assertEquals("whatsapp", steps[1].request.arguments["app"])
    }

    @Test
    fun multiAction_flashlightOnThenOff() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Turn flashlight on then off"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(2, steps.size)
        assertEquals("FLASHLIGHT", steps[0].request.toolId)
        assertEquals("ON", steps[0].request.arguments["action"])
        assertEquals("FLASHLIGHT", steps[1].request.toolId)
        assertEquals("OFF", steps[1].request.arguments["action"])
    }

    @Test
    fun multiAction_increaseBrightnessTo80AndVolumeTo60() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Increase brightness to 80 and volume to 60"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(2, steps.size)
        assertEquals("BRIGHTNESS", steps[0].request.toolId)
        assertEquals("SET", steps[0].request.arguments["action"])
        assertEquals("80", steps[0].request.arguments["value"])
        assertEquals("VOLUME", steps[1].request.toolId)
        assertEquals("SET", steps[1].request.arguments["action"])
        assertEquals("60", steps[1].request.arguments["value"])
    }

    @Test
    fun multiAction_openCameraThenGallery() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Open Camera then Gallery"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(2, steps.size)
        assertEquals("OPEN_APP", steps[0].request.toolId)
        assertEquals("camera", steps[0].request.arguments["app"])
        assertEquals("OPEN_APP", steps[1].request.toolId)
        assertEquals("gallery", steps[1].request.arguments["app"])
    }

    @Test
    fun singleAction_reduceBrightnessBy20Percent() {
        val result = planner.plan(
            history = ConversationHistory(),
            latestUserMessage = "Reduce brightness by 20%"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(1, steps.size)
        assertEquals("BRIGHTNESS", steps[0].request.toolId)
        assertEquals("DECREASE", steps[0].request.arguments["action"])
        assertEquals("20", steps[0].request.arguments["value"])
    }

    @Test
    fun followUp_closeIt_resolvesChrome() {
        val history = ConversationHistory(
            messages = listOf(
                ConversationMessage(role = ConversationRole.USER, text = "Open Chrome"),
                ConversationMessage(role = ConversationRole.ASSISTANT, text = "Opened Chrome")
            )
        )

        val result = planner.plan(
            history = history,
            latestUserMessage = "Close it"
        )

        assertTrue(result.decision is PlannerDecision.ContinueToGemini)
        assertEquals(IntentCategory.CLOSE_APP, result.intentResult.intent)
        assertEquals("chrome", result.intentResult.slots["app"])
    }

    @Test
    fun followUp_increaseItBy10Percent_targetsBrightness() {
        val history = ConversationHistory(
            messages = listOf(
                ConversationMessage(role = ConversationRole.USER, text = "Set brightness to 70%"),
                ConversationMessage(role = ConversationRole.ASSISTANT, text = "Brightness set to 70%")
            )
        )

        val result = planner.plan(
            history = history,
            latestUserMessage = "Increase it by 10%"
        )

        assertTrue(result.decision is PlannerDecision.Tool)
        val tool = result.decision as PlannerDecision.Tool
        val steps = parser.parsePlan(tool.structuredOutput, caller = "TEST")!!
        assertEquals(1, steps.size)
        assertEquals("BRIGHTNESS", steps[0].request.toolId)
        assertEquals("INCREASE", steps[0].request.arguments["action"])
        assertEquals("10", steps[0].request.arguments["value"])
    }
}
