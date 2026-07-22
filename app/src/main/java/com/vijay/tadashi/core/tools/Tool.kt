package com.vijay.tadashi.core.tools

interface Tool {
    val id: String
    val displayName: String
    val description: String
    val requiredPermissions: List<ToolPermission>
    val category: ToolCategory

    suspend fun execute(request: ToolRequest): ToolResult
}

