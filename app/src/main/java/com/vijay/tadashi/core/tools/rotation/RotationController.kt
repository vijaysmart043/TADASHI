package com.vijay.tadashi.core.tools.rotation

data class RotationStatusResult(
    val enabled: Boolean?,
    val message: String,
    val error: String? = null
)

data class RotationChangeResult(
    val verification: Boolean,
    val enabled: Boolean?,
    val requiresUserAction: Boolean,
    val message: String,
    val error: String? = null
)

interface RotationController {
    fun status(): RotationStatusResult
    fun setAutoRotateEnabled(enabled: Boolean): RotationChangeResult
}

