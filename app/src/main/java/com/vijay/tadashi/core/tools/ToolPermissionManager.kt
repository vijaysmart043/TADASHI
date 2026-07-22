package com.vijay.tadashi.core.tools

import javax.inject.Inject
import javax.inject.Singleton

sealed interface ToolPermissionStatus {
    data object Granted : ToolPermissionStatus

    data class Denied(
        val permissions: List<ToolPermission>
    ) : ToolPermissionStatus

    data class RequiresRuntimePermission(
        val permissions: List<ToolPermission>
    ) : ToolPermissionStatus
}

interface ToolPermissionManager {
    fun checkPermissions(requiredPermissions: List<ToolPermission>): ToolPermissionStatus
}

@Singleton
class DefaultToolPermissionManager @Inject constructor() : ToolPermissionManager {
    override fun checkPermissions(requiredPermissions: List<ToolPermission>): ToolPermissionStatus {
        if (requiredPermissions.isEmpty()) return ToolPermissionStatus.Granted

        val runtime = requiredPermissions.filter { it.requiresRuntimeGrant }
        if (runtime.isNotEmpty()) {
            return ToolPermissionStatus.RequiresRuntimePermission(runtime)
        }

        return ToolPermissionStatus.Denied(requiredPermissions)
    }
}

