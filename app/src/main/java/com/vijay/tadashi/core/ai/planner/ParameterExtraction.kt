package com.vijay.tadashi.core.ai.planner

data class ParameterExtraction(
    val toolId: String,
    val arguments: Map<String, String>,
    val reason: String
)

