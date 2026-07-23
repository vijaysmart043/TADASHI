package com.vijay.tadashi.core.tools

data class PlannedToolStep(
    val order: Int,
    val request: ToolRequest,
    val optional: Boolean = false,
    val intent: String? = null,
    val confidence: Double? = null,
    val reason: String? = null
)

