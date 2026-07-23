package com.vijay.tadashi.core.tools.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceInfoProvider {
    override fun getDeviceInfo(): Result<DeviceInfo> {
        return runCatching {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo().also { info ->
                activityManager?.getMemoryInfo(info)
            }
            val totalRamBytes = memInfo.totalMem.takeIf { it > 0L }

            val statFs = StatFs(Environment.getDataDirectory().absolutePath)
            val totalStorageBytes = statFs.totalBytes.takeIf { it > 0L }

            val dm = context.resources.displayMetrics
            val resolution = "${dm.widthPixels}x${dm.heightPixels}"

            DeviceInfo(
                model = Build.MODEL.orEmpty(),
                manufacturer = Build.MANUFACTURER.orEmpty(),
                androidVersion = Build.VERSION.RELEASE.orEmpty(),
                apiLevel = Build.VERSION.SDK_INT,
                totalRamBytes = totalRamBytes,
                totalStorageBytes = totalStorageBytes,
                cpuAbis = Build.SUPPORTED_ABIS.toList(),
                screenResolution = resolution
            )
        }
    }
}

