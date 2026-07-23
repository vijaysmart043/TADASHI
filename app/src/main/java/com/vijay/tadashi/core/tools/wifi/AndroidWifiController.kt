package com.vijay.tadashi.core.tools.wifi

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidWifiController @Inject constructor(
    @ApplicationContext private val context: Context
) : WifiController {
    @Suppress("DEPRECATION")
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    override fun isEnabled(): Result<Boolean> {
        return runCatching {
            val manager = wifiManager ?: return@runCatching false
            manager.isWifiEnabled
        }
    }

    @Suppress("DEPRECATION")
    override fun setEnabled(enabled: Boolean): WifiChangeResult {
        val manager = wifiManager
            ?: return WifiChangeResult(
                verification = false,
                enabled = null,
                requiresUserAction = false,
                message = "Wi-Fi manager not available",
                error = "WIFI_SERVICE unavailable"
            )

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openWifiPanel()
                val current = isEnabled().getOrNull()
                WifiChangeResult(
                    verification = current == enabled,
                    enabled = current,
                    requiresUserAction = true,
                    message = "Android restricts changing Wi-Fi programmatically. Opened Wi-Fi settings panel."
                )
            } else {
                val requested = manager.setWifiEnabled(enabled)
                val current = manager.isWifiEnabled
                WifiChangeResult(
                    verification = requested && current == enabled,
                    enabled = current,
                    requiresUserAction = false,
                    message = if (current == enabled) {
                        if (enabled) "Wi-Fi turned on" else "Wi-Fi turned off"
                    } else {
                        "Wi-Fi change request did not apply"
                    }
                )
            }
        }.getOrElse { e ->
            WifiChangeResult(
                verification = false,
                enabled = isEnabled().getOrNull(),
                requiresUserAction = false,
                message = "Wi-Fi operation failed",
                error = e.message ?: e::class.java.simpleName
            )
        }
    }

    private fun openWifiPanel() {
        runCatching {
            val intent = Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

