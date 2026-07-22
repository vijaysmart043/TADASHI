package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import com.vijay.tadashi.core.tools.ToolRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIToolPlanner @Inject constructor() {
    private val rules: List<PlannerRule> = listOf(
        OpenAppRule(),
        FlashlightRule()
    )

    fun plan(
        history: ConversationHistory,
        latestUserMessage: String
    ): PlannerResult {
        PlannerLogger.d("Planner started")

        val normalized = normalize(latestUserMessage)
        val match = rules.firstNotNullOfOrNull { rule ->
            rule.match(history = history, normalizedMessage = normalized)?.let { decision ->
                PlannerLogger.d("Matched ${rule.id}")
                PlannerResult(
                    decision = decision,
                    matchedRuleId = rule.id,
                    normalizedMessage = normalized
                )
            }
        }

        if (match != null) {
            PlannerLogger.d("Detected Tool Request")
            return match
        }

        PlannerLogger.d("Detected Chat Request")
        return PlannerResult(
            decision = PlannerDecision.ContinueToGemini,
            matchedRuleId = null,
            normalizedMessage = normalized
        )
    }

    private fun normalize(raw: String): String {
        return raw
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .trim('.', '!', '?', ',', ':', ';')
    }

    private class OpenAppRule : PlannerRule {
        override val id: String = "OPEN_APP"

        private val openRegex = Regex("^(open|launch|start)\\s+(the\\s+)?(.+)$")

        override fun match(
            history: ConversationHistory,
            normalizedMessage: String
        ): PlannerDecision.Tool? {
            val match = openRegex.find(normalizedMessage) ?: return null
            val app = match.groupValues.getOrNull(3)
                ?.trim()
                ?.trim('.', '!', '?', ',', ':', ';')
                ?.takeIf { it.isNotBlank() }
                ?: return null

            return PlannerDecision.Tool(
                request = ToolRequest(
                    toolId = "OPEN_APP",
                    arguments = mapOf("app" to app),
                    caller = "PLANNER",
                    timestampMs = System.currentTimeMillis()
                )
            )
        }
    }

    private class FlashlightRule : PlannerRule {
        override val id: String = "FLASHLIGHT"

        private val onPatterns = listOf(
            Regex("^turn on (the )?(flashlight|torch|light)$"),
            Regex("^(flashlight|torch|light) on$"),
            Regex("^switch on (the )?(flashlight|torch|light)$")
        )

        private val offPatterns = listOf(
            Regex("^turn off (the )?(flashlight|torch|light)$"),
            Regex("^(flashlight|torch|light) off$"),
            Regex("^switch off (the )?(flashlight|torch|light)$")
        )

        private val togglePatterns = listOf(
            Regex("^toggle (the )?(flashlight|torch|light)$")
        )

        override fun match(
            history: ConversationHistory,
            normalizedMessage: String
        ): PlannerDecision.Tool? {
            val action = when {
                togglePatterns.any { it.matches(normalizedMessage) } -> "TOGGLE"
                onPatterns.any { it.matches(normalizedMessage) } -> "ON"
                offPatterns.any { it.matches(normalizedMessage) } -> "OFF"
                normalizedMessage.contains("toggle") && mentionsLight(normalizedMessage) -> "TOGGLE"
                normalizedMessage.contains("turn on") && mentionsLight(normalizedMessage) -> "ON"
                normalizedMessage.contains("turn off") && mentionsLight(normalizedMessage) -> "OFF"
                else -> null
            } ?: return null

            return PlannerDecision.Tool(
                request = ToolRequest(
                    toolId = "FLASHLIGHT",
                    arguments = mapOf("action" to action),
                    caller = "PLANNER",
                    timestampMs = System.currentTimeMillis()
                )
            )
        }

        private fun mentionsLight(message: String): Boolean {
            return message.contains("flashlight") || message.contains("torch") || message.contains("light")
        }
    }
}
