package com.vijay.tadashi.core.ai.planner

data class IntentResult(
    val intent: IntentCategory,
    val confidence: Double,
    val reason: String,
    val slots: Map<String, String> = emptyMap()
)
