package com.vijay.tadashi.core.tools

sealed interface ToolPermission {
    val id: String
    val requiresRuntimeGrant: Boolean
}

data class RuntimeToolPermission(
    override val id: String
) : ToolPermission {
    override val requiresRuntimeGrant: Boolean = true
}

data class InternalToolPermission(
    override val id: String
) : ToolPermission {
    override val requiresRuntimeGrant: Boolean = false
}

