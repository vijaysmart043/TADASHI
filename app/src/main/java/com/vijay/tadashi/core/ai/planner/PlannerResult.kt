package com.vijay.tadashi.core.ai.planner

data class PlannerResult(
    val intentResult: IntentResult,
    val decision: PlannerDecision,
    val normalizedMessage: String,
    val executionPlan: ExecutionPlan? = null,
    val planningTimeMs: Long = 0L
)
