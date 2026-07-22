package com.vijay.tadashi.di

import com.vijay.tadashi.core.tools.AlarmTool
import com.vijay.tadashi.core.tools.BrightnessTool
import com.vijay.tadashi.core.tools.CallTool
import com.vijay.tadashi.core.tools.CameraTool
import com.vijay.tadashi.core.tools.ClipboardTool
import com.vijay.tadashi.core.tools.DefaultToolPermissionManager
import com.vijay.tadashi.core.tools.flashlight.FlashlightTool
import com.vijay.tadashi.core.tools.apps.DefaultInstalledAppsRepository
import com.vijay.tadashi.core.tools.apps.AppLauncherTool
import com.vijay.tadashi.core.tools.apps.InstalledAppsRepository
import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolPermissionManager
import com.vijay.tadashi.core.tools.VolumeTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolsModule {
    @Provides
    @Singleton
    fun provideToolPermissionManager(
        impl: DefaultToolPermissionManager
    ): ToolPermissionManager = impl

    @Provides
    @Singleton
    fun provideInstalledAppsRepository(
        impl: DefaultInstalledAppsRepository
    ): InstalledAppsRepository = impl

    @Provides
    @IntoSet
    fun provideAppLauncherTool(tool: AppLauncherTool): Tool = tool

    @Provides
    @IntoSet
    fun provideFlashlightTool(tool: FlashlightTool): Tool = tool

    @Provides
    @IntoSet
    fun provideVolumeTool(tool: VolumeTool): Tool = tool

    @Provides
    @IntoSet
    fun provideBrightnessTool(tool: BrightnessTool): Tool = tool

    @Provides
    @IntoSet
    fun provideCameraTool(tool: CameraTool): Tool = tool

    @Provides
    @IntoSet
    fun provideCallTool(tool: CallTool): Tool = tool

    @Provides
    @IntoSet
    fun provideAlarmTool(tool: AlarmTool): Tool = tool

    @Provides
    @IntoSet
    fun provideClipboardTool(tool: ClipboardTool): Tool = tool
}
