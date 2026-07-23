package com.vijay.tadashi.di

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.battery.AndroidBatteryInfoProvider
import com.vijay.tadashi.core.tools.battery.BatteryInfoProvider
import com.vijay.tadashi.core.tools.battery.BatteryTool
import com.vijay.tadashi.core.tools.bluetooth.AndroidBluetoothController
import com.vijay.tadashi.core.tools.bluetooth.BluetoothController
import com.vijay.tadashi.core.tools.bluetooth.BluetoothTool
import com.vijay.tadashi.core.tools.clipboard.AndroidClipboardController
import com.vijay.tadashi.core.tools.clipboard.ClipboardController
import com.vijay.tadashi.core.tools.clipboard.ClipboardTool
import com.vijay.tadashi.core.tools.deviceinfo.AndroidDeviceInfoProvider
import com.vijay.tadashi.core.tools.deviceinfo.DeviceInfoProvider
import com.vijay.tadashi.core.tools.deviceinfo.DeviceInfoTool
import com.vijay.tadashi.core.tools.ringer.AndroidRingerController
import com.vijay.tadashi.core.tools.ringer.RingerController
import com.vijay.tadashi.core.tools.ringer.RingerModeTool
import com.vijay.tadashi.core.tools.rotation.AndroidRotationController
import com.vijay.tadashi.core.tools.rotation.RotationController
import com.vijay.tadashi.core.tools.rotation.ScreenRotationTool
import com.vijay.tadashi.core.tools.wifi.AndroidWifiController
import com.vijay.tadashi.core.tools.wifi.WifiController
import com.vijay.tadashi.core.tools.wifi.WifiTool
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemToolsBindingsModule {
    @Binds
    abstract fun bindWifiController(impl: AndroidWifiController): WifiController

    @Binds
    abstract fun bindBluetoothController(impl: AndroidBluetoothController): BluetoothController

    @Binds
    abstract fun bindBatteryInfoProvider(impl: AndroidBatteryInfoProvider): BatteryInfoProvider

    @Binds
    abstract fun bindDeviceInfoProvider(impl: AndroidDeviceInfoProvider): DeviceInfoProvider

    @Binds
    abstract fun bindClipboardController(impl: AndroidClipboardController): ClipboardController

    @Binds
    abstract fun bindRingerController(impl: AndroidRingerController): RingerController

    @Binds
    abstract fun bindRotationController(impl: AndroidRotationController): RotationController
}

@Module
@InstallIn(SingletonComponent::class)
object SystemToolsModule {
    @Provides
    @IntoSet
    fun provideWifiTool(tool: WifiTool): Tool = tool

    @Provides
    @IntoSet
    fun provideBluetoothTool(tool: BluetoothTool): Tool = tool

    @Provides
    @IntoSet
    fun provideBatteryTool(tool: BatteryTool): Tool = tool

    @Provides
    @IntoSet
    fun provideDeviceInfoTool(tool: DeviceInfoTool): Tool = tool

    @Provides
    @IntoSet
    fun provideClipboardTool(tool: ClipboardTool): Tool = tool

    @Provides
    @IntoSet
    fun provideRingerModeTool(tool: RingerModeTool): Tool = tool

    @Provides
    @IntoSet
    fun provideScreenRotationTool(tool: ScreenRotationTool): Tool = tool
}

