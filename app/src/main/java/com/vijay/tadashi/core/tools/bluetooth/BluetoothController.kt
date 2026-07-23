package com.vijay.tadashi.core.tools.bluetooth

data class BluetoothStatusResult(
    val enabled: Boolean?,
    val requiresPermission: Boolean,
    val requiredPermission: String? = null,
    val message: String,
    val error: String? = null
)

data class BluetoothChangeResult(
    val verification: Boolean,
    val enabled: Boolean?,
    val requiresPermission: Boolean,
    val requiredPermission: String? = null,
    val requiresUserAction: Boolean,
    val message: String,
    val error: String? = null
)

interface BluetoothController {
    fun status(): BluetoothStatusResult
    fun setEnabled(enabled: Boolean): BluetoothChangeResult
}

