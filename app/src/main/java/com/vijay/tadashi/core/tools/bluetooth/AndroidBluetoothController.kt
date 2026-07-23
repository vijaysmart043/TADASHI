package com.vijay.tadashi.core.tools.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBluetoothController @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothController {
    private val adapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    override fun status(): BluetoothStatusResult {
        val btAdapter = adapter
            ?: return BluetoothStatusResult(
                enabled = null,
                requiresPermission = false,
                message = "Bluetooth not supported on this device",
                error = "NO_BLUETOOTH_ADAPTER"
            )

        val permission = requiredConnectPermission()
        if (permission != null && !hasPermission(permission)) {
            return BluetoothStatusResult(
                enabled = null,
                requiresPermission = true,
                requiredPermission = permission,
                message = "Bluetooth permission required: $permission",
                error = "MISSING_PERMISSION"
            )
        }

        return runCatching {
            BluetoothStatusResult(
                enabled = btAdapter.isEnabled,
                requiresPermission = false,
                message = if (btAdapter.isEnabled) "Bluetooth is ON" else "Bluetooth is OFF",
                error = null
            )
        }.getOrElse { e ->
            BluetoothStatusResult(
                enabled = null,
                requiresPermission = false,
                message = "Failed to read Bluetooth status",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun setEnabled(enabled: Boolean): BluetoothChangeResult {
        val btAdapter = adapter
            ?: return BluetoothChangeResult(
                verification = false,
                enabled = null,
                requiresPermission = false,
                requiresUserAction = false,
                message = "Bluetooth not supported on this device",
                error = "NO_BLUETOOTH_ADAPTER"
            )

        val permission = requiredConnectPermission()
        if (permission != null && !hasPermission(permission)) {
            return BluetoothChangeResult(
                verification = false,
                enabled = null,
                requiresPermission = true,
                requiredPermission = permission,
                requiresUserAction = false,
                message = "Bluetooth permission required: $permission",
                error = "MISSING_PERMISSION"
            )
        }

        return runCatching {
            val applied = if (enabled) btAdapter.enable() else btAdapter.disable()
            val current = runCatching { btAdapter.isEnabled }.getOrNull()

            if (!applied) {
                openBluetoothPanel()
                return@runCatching BluetoothChangeResult(
                    verification = current == enabled,
                    enabled = current,
                    requiresPermission = false,
                    requiresUserAction = true,
                    message = "Android restricted direct Bluetooth control. Opened Bluetooth settings panel.",
                    error = "USER_ACTION_REQUIRED"
                )
            }

            BluetoothChangeResult(
                verification = current == enabled,
                enabled = current,
                requiresPermission = false,
                requiresUserAction = false,
                message = if (current == enabled) {
                    if (enabled) "Bluetooth turned on" else "Bluetooth turned off"
                } else {
                    "Bluetooth change request did not apply"
                },
                error = null
            )
        }.getOrElse { e ->
            BluetoothChangeResult(
                verification = false,
                enabled = runCatching { btAdapter.isEnabled }.getOrNull(),
                requiresPermission = false,
                requiresUserAction = false,
                message = "Bluetooth operation failed",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    private fun requiredConnectPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            null
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun openBluetoothPanel() {
        runCatching {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
