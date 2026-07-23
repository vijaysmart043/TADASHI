package com.vijay.tadashi.core.tools.battery

data class BatteryInfo(
    val percentage: Int?,
    val isCharging: Boolean?,
    val health: String?,
    val temperatureC: Float?,
    val powerSave: Boolean?
)

interface BatteryInfoProvider {
    fun getBatteryInfo(): Result<BatteryInfo>
}

