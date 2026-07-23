package com.vijay.tadashi.di

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.alarmtimer.AlarmTimerController
import com.vijay.tadashi.core.tools.alarmtimer.AlarmTimerTool
import com.vijay.tadashi.core.tools.alarmtimer.AndroidAlarmTimerController
import com.vijay.tadashi.core.tools.calendar.AndroidCalendarController
import com.vijay.tadashi.core.tools.calendar.CalendarController
import com.vijay.tadashi.core.tools.calendar.CalendarTool
import com.vijay.tadashi.core.tools.camera.AndroidCameraController
import com.vijay.tadashi.core.tools.camera.CameraController
import com.vijay.tadashi.core.tools.camera.CameraInteractionTool
import com.vijay.tadashi.core.tools.gallery.AndroidGalleryController
import com.vijay.tadashi.core.tools.gallery.GalleryController
import com.vijay.tadashi.core.tools.gallery.GalleryTool
import com.vijay.tadashi.core.tools.media.AndroidMediaControlController
import com.vijay.tadashi.core.tools.media.MediaControlController
import com.vijay.tadashi.core.tools.media.MediaControlTool
import com.vijay.tadashi.core.tools.notifications.AndroidNotificationController
import com.vijay.tadashi.core.tools.notifications.NotificationController
import com.vijay.tadashi.core.tools.notifications.NotificationTool
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class InteractionToolsBindingsModule {
    @Binds
    abstract fun bindNotificationController(impl: AndroidNotificationController): NotificationController

    @Binds
    abstract fun bindMediaControlController(impl: AndroidMediaControlController): MediaControlController

    @Binds
    abstract fun bindCameraController(impl: AndroidCameraController): CameraController

    @Binds
    abstract fun bindGalleryController(impl: AndroidGalleryController): GalleryController

    @Binds
    abstract fun bindAlarmTimerController(impl: AndroidAlarmTimerController): AlarmTimerController

    @Binds
    abstract fun bindCalendarController(impl: AndroidCalendarController): CalendarController
}

@Module
@InstallIn(SingletonComponent::class)
object InteractionToolsModule {
    @Provides
    @IntoSet
    fun provideNotificationTool(tool: NotificationTool): Tool = tool

    @Provides
    @IntoSet
    fun provideMediaControlTool(tool: MediaControlTool): Tool = tool

    @Provides
    @IntoSet
    fun provideCameraInteractionTool(tool: CameraInteractionTool): Tool = tool

    @Provides
    @IntoSet
    fun provideGalleryTool(tool: GalleryTool): Tool = tool

    @Provides
    @IntoSet
    fun provideAlarmTimerTool(tool: AlarmTimerTool): Tool = tool

    @Provides
    @IntoSet
    fun provideCalendarTool(tool: CalendarTool): Tool = tool
}

