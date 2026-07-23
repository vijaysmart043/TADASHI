package com.vijay.tadashi.core.ai.planner

sealed interface PlannerDecision {
    data class Tool(
        val structuredOutput: String
    ) : PlannerDecision

    data object ContinueToGemini : PlannerDecision
}
