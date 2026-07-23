package com.vijay.tadashi.core.tools.interaction

import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.alarmtimer.AlarmTimerController
import com.vijay.tadashi.core.tools.alarmtimer.AlarmTimerTool
import com.vijay.tadashi.core.tools.calendar.CalendarController
import com.vijay.tadashi.core.tools.calendar.CalendarEventDraft
import com.vijay.tadashi.core.tools.calendar.CalendarEventSummary
import com.vijay.tadashi.core.tools.calendar.CalendarTool
import com.vijay.tadashi.core.tools.camera.CameraController
import com.vijay.tadashi.core.tools.camera.CameraInteractionTool
import com.vijay.tadashi.core.tools.common.ControllerResult
import com.vijay.tadashi.core.tools.gallery.GalleryController
import com.vijay.tadashi.core.tools.gallery.GalleryTool
import com.vijay.tadashi.core.tools.media.MediaAction
import com.vijay.tadashi.core.tools.media.MediaControlController
import com.vijay.tadashi.core.tools.media.MediaControlTool
import com.vijay.tadashi.core.tools.media.MediaStatus
import com.vijay.tadashi.core.tools.notifications.ActiveNotification
import com.vijay.tadashi.core.tools.notifications.NotificationController
import com.vijay.tadashi.core.tools.notifications.NotificationTool
import com.vijay.tadashi.core.tools.system.ToolExecutionReport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractionToolsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `read notifications`() = runBlocking {
        val controller = object : NotificationController {
            override fun isNotificationAccessEnabled(): Boolean = true

            override fun readActiveNotifications(packageOrAppFilter: String?): ControllerResult<List<ActiveNotification>> {
                return ControllerResult.Success(
                    value = listOf(
                        ActiveNotification(
                            key = "1",
                            packageName = "com.whatsapp",
                            title = "WhatsApp",
                            text = "Hello",
                            postTimeMs = 2L,
                            isClearable = true
                        )
                    ),
                    permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
                )
            }

            override fun clearDismissibleNotifications(packageOrAppFilter: String?): ControllerResult<Int> {
                return ControllerResult.Success(1, permissionStatus = "NOTIFICATION_ACCESS_GRANTED")
            }
        }

        val tool = NotificationTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "READ"),
                caller = "test",
                timestampMs = 0L
            )
        )

        val report = decodeReport(result)
        assertTrue(report.success)
        assertTrue(report.verification)
        assertTrue(report.message.contains("notifications"))
    }

    @Test
    fun `pause music`() = runBlocking {
        val controller = object : MediaControlController {
            override fun sendMediaAction(action: MediaAction): ControllerResult<Unit> {
                assertEquals(MediaAction.PAUSE, action)
                return ControllerResult.Success(Unit)
            }

            override fun getCurrentStatus(): ControllerResult<MediaStatus> {
                return ControllerResult.Success(MediaStatus(null, null, null))
            }
        }

        val tool = MediaControlTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "PAUSE"),
                caller = "test",
                timestampMs = 0L
            )
        )

        val report = decodeReport(result)
        assertTrue(report.success)
        assertEquals("Paused", report.message)
    }

    @Test
    fun `next song`() = runBlocking {
        val controller = object : MediaControlController {
            override fun sendMediaAction(action: MediaAction): ControllerResult<Unit> {
                assertEquals(MediaAction.NEXT, action)
                return ControllerResult.Success(Unit)
            }

            override fun getCurrentStatus(): ControllerResult<MediaStatus> {
                return ControllerResult.Success(MediaStatus(null, null, null))
            }
        }

        val tool = MediaControlTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "NEXT"),
                caller = "test",
                timestampMs = 0L
            )
        )

        val report = decodeReport(result)
        assertTrue(report.success)
        assertEquals("Next track", report.message)
    }

    @Test
    fun `open camera`() = runBlocking {
        val controller = object : CameraController {
            override fun openCamera(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun openVideoMode(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun takePicture(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun switchCamera(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
        }
        val tool = CameraInteractionTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "OPEN"),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    @Test
    fun `take photo`() = runBlocking {
        val controller = object : CameraController {
            override fun openCamera(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun openVideoMode(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun takePicture(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun switchCamera(lensFacing: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
        }
        val tool = CameraInteractionTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "TAKE_PICTURE"),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    @Test
    fun `open gallery`() = runBlocking {
        val controller = object : GalleryController {
            override fun openGallery(): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun openLatestImage(): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun shareLatestImage(): ControllerResult<Unit> = ControllerResult.Success(Unit)
        }
        val tool = GalleryTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "OPEN"),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    @Test
    fun `set alarm`() = runBlocking {
        val controller = object : AlarmTimerController {
            override fun setAlarm(hour: Int, minute: Int, label: String?): ControllerResult<Unit> {
                assertEquals(7, hour)
                assertEquals(0, minute)
                return ControllerResult.Success(Unit)
            }

            override fun cancelAlarm(label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun setTimer(lengthSeconds: Int, label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
            override fun cancelTimer(label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
        }
        val tool = AlarmTimerTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "SET_ALARM", "time" to "7 AM"),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    @Test
    fun `set timer`() = runBlocking {
        val controller = object : AlarmTimerController {
            override fun setAlarm(hour: Int, minute: Int, label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)

            override fun cancelAlarm(label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)

            override fun setTimer(lengthSeconds: Int, label: String?): ControllerResult<Unit> {
                assertEquals(600, lengthSeconds)
                return ControllerResult.Success(Unit)
            }

            override fun cancelTimer(label: String?): ControllerResult<Unit> = ControllerResult.Success(Unit)
        }
        val tool = AlarmTimerTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf("action" to "SET_TIMER", "duration" to "10 minutes"),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    @Test
    fun `create calendar event`() = runBlocking {
        val controller = object : CalendarController {
            override fun openCalendar(): ControllerResult<Unit> = ControllerResult.Success(Unit)

            override fun createEvent(draft: CalendarEventDraft): ControllerResult<Unit> {
                assertEquals("Meeting", draft.title)
                return ControllerResult.Success(Unit)
            }

            override fun getTodayEvents(
                startOfDayMs: Long,
                endOfDayMs: Long
            ): ControllerResult<List<CalendarEventSummary>> {
                return ControllerResult.Success(emptyList())
            }
        }
        val tool = CalendarTool(controller)
        val result = tool.execute(
            ToolRequest(
                toolId = tool.id,
                arguments = mapOf(
                    "action" to "CREATE_EVENT",
                    "title" to "Meeting",
                    "startTimeMs" to "1000",
                    "endTimeMs" to "2000"
                ),
                caller = "test",
                timestampMs = 0L
            )
        )
        val report = decodeReport(result)
        assertTrue(report.success)
    }

    private fun decodeReport(result: ToolResult): ToolExecutionReport {
        val raw = (result as ToolResult.Success).message
        return json.decodeFromString(raw)
    }
}

