package com.vijay.tadashi.core.ai.planner

data class ExecutionPlan(
    val actions: List<PlannedAction>
)

data class PlannedAction(
    val order: Int,
    val intent: IntentCategory,
    val toolId: String,
    val confidence: Double,
    val parameters: Map<String, String>,
    val reason: String,
    val optional: Boolean = false
)

