package com.vijay.tadashi.core.tools.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBatteryInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryInfoProvider {
    override fun getBatteryInfo(): Result<BatteryInfo> {
        return runCatching {
            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percentage = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
            } else {
                null
            }

            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL -> true
                BatteryManager.BATTERY_STATUS_DISCHARGING,
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
                else -> null
            }

            val health = when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
                BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER_VOLTAGE"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "UNSPECIFIED_FAILURE"
                BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
                else -> null
            }

            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                ?: Int.MIN_VALUE
            val temperatureC = if (tempTenths != Int.MIN_VALUE) {
                tempTenths / 10f
            } else {
                null
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val powerSave = powerManager?.isPowerSaveMode

            BatteryInfo(
                percentage = percentage,
                isCharging = isCharging,
                health = health,
                temperatureC = temperatureC,
                powerSave = powerSave
            )
        }
    }
}

