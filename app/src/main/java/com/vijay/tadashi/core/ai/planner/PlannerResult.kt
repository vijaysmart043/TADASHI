package com.vijay.tadashi.core.ai.planner

data class PlannerResult(
    val decision: PlannerDecision,
    val matchedRuleId: String? = null,
    val normalizedMessage: String
)
