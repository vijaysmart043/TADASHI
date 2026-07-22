package com.vijay.tadashi.core.tools

sealed interface ToolResult {
    data class Success(
        val message: String
    ) : ToolResult

    data class Failure(
        val message: String
    ) : ToolResult

    data class PermissionDenied(
        val status: ToolPermissionStatus
    ) : ToolResult

    data class Unsupported(
        val message: String
    ) : ToolResult

    data class Cancelled(
        val message: String
    ) : ToolResult
}

