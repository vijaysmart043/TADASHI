package com.vijay.tadashi.core.tools.deviceinfo

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val totalRamBytes: Long?,
    val totalStorageBytes: Long?,
    val cpuAbis: List<String>,
    val screenResolution: String?
)

interface DeviceInfoProvider {
    fun getDeviceInfo(): Result<DeviceInfo>
}

