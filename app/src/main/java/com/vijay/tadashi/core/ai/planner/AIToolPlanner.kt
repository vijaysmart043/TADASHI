package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.ai.conversation.ConversationHistory
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale

@Singleton
class AIToolPlanner @Inject constructor(
    private val classifier: IntentClassifier,
    private val parameterExtractor: ParameterExtractor
) {
    fun plan(
        history: ConversationHistory,
        latestUserMessage: String
    ): PlannerResult {
        val startMs = System.currentTimeMillis()
        PlannerLogger.d("Planner started")
        PlannerLogger.d("Input: $latestUserMessage")

        val normalized = normalize(latestUserMessage)
        val segments = MultiActionSegmenter.segment(normalized).map { seg ->
            seg.copy(text = PlannerMessageRewriter.rewriteSegment(history, seg.text))
        }

        PlannerLogger.d("Segments: ${segments.size}")
        segments.forEachIndexed { idx, seg ->
            PlannerLogger.d("Segment[${idx + 1}] (${seg.connectorBefore}): ${seg.text}")
        }

        val plan = buildExecutionPlan(history = history, segments = segments)
        val primaryIntent = plan?.actions?.firstOrNull()?.intent
        val intentResult = if (primaryIntent != null) {
            IntentResult(
                intent = primaryIntent,
                confidence = plan.actions.minOf { it.confidence },
                reason = "Multi-step plan (${plan.actions.size} actions)"
            )
        } else {
            classifier.classify(
                history = history,
                normalizedMessage = segments.firstOrNull()?.text ?: normalized
            )
        }

        PlannerLogger.d("Intent: ${intentResult.intent}")
        PlannerLogger.d("Confidence: ${"%.2f".format(intentResult.confidence)}")
        PlannerLogger.d("Reason: ${intentResult.reason}")

        val decision = if (plan != null && plan.actions.isNotEmpty()) {
            PlannerDecision.Tool(structuredOutput = buildStructuredPlanJson(plan.actions))
        } else {
            PlannerDecision.ContinueToGemini
        }
        when (decision) {
            is PlannerDecision.Tool -> PlannerLogger.d("Decision: TOOL")
            PlannerDecision.ContinueToGemini -> PlannerLogger.d("Decision: GEMINI")
        }

        val elapsed = System.currentTimeMillis() - startMs
        PlannerLogger.d("Action Count: ${plan?.actions?.size ?: 0}")
        PlannerLogger.d("Total Planning Time: ${elapsed}ms")

        return PlannerResult(
            intentResult = intentResult,
            decision = decision,
            normalizedMessage = normalized,
            executionPlan = plan,
            planningTimeMs = elapsed
        )
    }

    private fun normalize(raw: String): String {
        return raw
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .trim('.', '!', '?', ',', ':', ';')
    }

    private data class StepCandidate(
        val segment: String,
        val connectorBefore: MultiActionSegmenter.Connector,
        val intentResult: IntentResult,
        val extraction: ParameterExtraction
    )

    private fun buildExecutionPlan(
        history: ConversationHistory,
        segments: List<MultiActionSegmenter.ActionSegment>
    ): ExecutionPlan? {
        val supportedTools = setOf(
            "OPEN_APP",
            "FLASHLIGHT",
            "BRIGHTNESS",
            "VOLUME"
        )

        val candidates = mutableListOf<StepCandidate>()
        var previousIntent: IntentCategory? = null

        for (segment in segments) {
            val repaired = repairImplicitSegment(segment.text, previousIntent)
            val intentResult = classifier.classify(history = history, normalizedMessage = repaired)
            if (intentResult.confidence < PlannerConfig.TOOL_CONFIDENCE_THRESHOLD) {
                return null
            }

            val extraction = parameterExtractor.extract(
                normalizedMessage = repaired,
                intentResult = intentResult
            ) ?: return null

            if (extraction.toolId !in supportedTools) return null

            ParameterLogger.d(
                "Extracted tool=${extraction.toolId} args=${extraction.arguments} reason=${extraction.reason}"
            )

            candidates.add(
                StepCandidate(
                    segment = repaired,
                    connectorBefore = segment.connectorBefore,
                    intentResult = intentResult,
                    extraction = extraction
                )
            )

            previousIntent = intentResult.intent
        }

        val resolved = resolveConflicts(candidates)
        val actions = resolved.mapIndexed { idx, step ->
            PlannedAction(
                order = idx + 1,
                intent = step.intentResult.intent,
                toolId = step.extraction.toolId,
                confidence = step.intentResult.confidence,
                parameters = step.extraction.arguments,
                reason = listOf(step.intentResult.reason, step.extraction.reason).joinToString(separator = " | "),
                optional = false
            )
        }

        return ExecutionPlan(actions = actions)
    }

    private fun repairImplicitSegment(
        segment: String,
        previousIntent: IntentCategory?
    ): String {
        val s = segment.trim()
        if (s.isBlank() || previousIntent == null) return s

        val startsWithOpenVerb = Regex("^(open|launch|start|run|bring up)\\s+").containsMatchIn(s)
        val startsWithDeviceVerb = Regex("^(set|increase|decrease|reduce|lower|raise|mute)\\s+").containsMatchIn(s)

        return when {
            previousIntent == IntentCategory.OPEN_APP && !startsWithOpenVerb && !startsWithDeviceVerb ->
                "open $s"

            previousIntent == IntentCategory.FLASHLIGHT && (s == "on" || s == "off" || s == "toggle") ->
                "flashlight $s"

            previousIntent == IntentCategory.DEVICE_CONTROL && !startsWithDeviceVerb &&
                (s.startsWith("volume") || s.startsWith("brightness")) ->
                "set $s"

            else -> s
        }
    }

    private fun resolveConflicts(candidates: List<StepCandidate>): List<StepCandidate> {
        val out = mutableListOf<StepCandidate>()
        for (current in candidates) {
            val prev = out.lastOrNull()
            if (prev != null &&
                current.connectorBefore == MultiActionSegmenter.Connector.AND &&
                isConflict(prev, current)
            ) {
                out.removeLast()
                out.add(
                    current.copy(
                        intentResult = current.intentResult.copy(
                            reason = "${current.intentResult.reason} | Conflict resolved: replaced previous"
                        )
                    )
                )
            } else {
                out.add(current)
            }
        }
        return out
    }

    private fun isConflict(a: StepCandidate, b: StepCandidate): Boolean {
        if (a.extraction.toolId != b.extraction.toolId) return false

        val toolId = a.extraction.toolId
        val aAction = a.extraction.arguments["action"]?.uppercase()
        val bAction = b.extraction.arguments["action"]?.uppercase()

        return when (toolId) {
            "FLASHLIGHT" -> (aAction == "ON" && bAction == "OFF") || (aAction == "OFF" && bAction == "ON")
            "BRIGHTNESS",
            "VOLUME" -> {
                val aValue = a.extraction.arguments["value"]
                val bValue = b.extraction.arguments["value"]
                when {
                    aAction == "SET" && bAction == "SET" && aValue != null && bValue != null && aValue != bValue -> true
                    aAction == "INCREASE" && bAction == "DECREASE" -> true
                    aAction == "DECREASE" && bAction == "INCREASE" -> true
                    else -> false
                }
            }

            else -> false
        }
    }

    private fun buildStructuredPlanJson(actions: List<PlannedAction>): String {
        val stepsJson = actions.joinToString(separator = ",") { action ->
            val argsJson = action.parameters.entries.joinToString(separator = ",") { (k, v) ->
                "\"${escapeJson(k)}\":\"${escapeJson(v)}\""
            }

            val confidence = String.format(Locale.US, "%.3f", action.confidence)
            """{"order":${action.order},"intent":"${escapeJson(action.intent.name)}","tool":"${escapeJson(action.toolId)}","confidence":$confidence,"reason":"${escapeJson(action.reason)}","optional":${action.optional},"arguments":{$argsJson}}"""
        }
        return """{"version":"2.0","steps":[$stepsJson]}"""
    }

    private fun escapeJson(raw: String): String {
        return raw
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
