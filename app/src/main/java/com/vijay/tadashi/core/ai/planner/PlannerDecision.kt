package com.vijay.tadashi.core.ai.planner

import com.vijay.tadashi.core.tools.ToolRequest

sealed interface PlannerDecision {
    data class Tool(
        val request: ToolRequest
    ) : PlannerDecision

    data object ContinueToGemini : PlannerDecision
}
