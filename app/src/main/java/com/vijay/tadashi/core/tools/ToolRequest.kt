package com.vijay.tadashi.core.tools

data class ToolRequest(
    val toolId: String,
    val arguments: Map<String, String>,
    val caller: String,
    val timestampMs: Long
)

