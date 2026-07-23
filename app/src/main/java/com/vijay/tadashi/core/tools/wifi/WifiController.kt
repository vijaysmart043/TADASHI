package com.vijay.tadashi.core.tools.wifi

data class WifiChangeResult(
    val verification: Boolean,
    val enabled: Boolean?,
    val requiresUserAction: Boolean,
    val message: String,
    val error: String? = null
)

interface WifiController {
    fun isEnabled(): Result<Boolean>
    fun setEnabled(enabled: Boolean): WifiChangeResult
}

