package com.vijay.tadashi.core.tools.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.vijay.tadashi.core.tools.common.ControllerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCalendarController @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarController {
    override fun openCalendar(): ControllerResult<Unit> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return start(intent, "OPEN_CALENDAR_FAILED")
    }

    override fun createEvent(draft: CalendarEventDraft): ControllerResult<Unit> {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(CalendarContract.Events.TITLE, draft.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, draft.startTimeMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, draft.endTimeMs)
            draft.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
            draft.description?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
        }
        return start(intent, "CREATE_EVENT_FAILED")
    }

    override fun getTodayEvents(
        startOfDayMs: Long,
        endOfDayMs: Long
    ): ControllerResult<List<CalendarEventSummary>> {
        if (!hasReadCalendarPermission()) {
            return ControllerResult.PermissionDenied(
                permissionStatus = "READ_CALENDAR_DENIED",
                message = "Missing permission to read calendar"
            )
        }

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END
        )
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, startOfDayMs)
            ContentUris.appendId(this, endOfDayMs)
        }.build()

        val events = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->
            val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CalendarEventSummary(
                            title = cursor.getString(titleIdx),
                            startTimeMs = cursor.getLong(beginIdx),
                            endTimeMs = cursor.getLong(endIdx)
                        )
                    )
                }
            }
        } ?: emptyList()

        return ControllerResult.Success(
            value = events,
            permissionStatus = "READ_CALENDAR_GRANTED"
        )
    }

    private fun start(intent: Intent, errorCode: String): ControllerResult<Unit> {
        val resolved = intent.resolveActivity(context.packageManager) != null
        if (!resolved) {
            return ControllerResult.Failure(
                message = "No application can handle this request",
                error = "NO_HANDLER",
                permissionStatus = permissionStatus()
            )
        }
        return runCatching {
            context.startActivity(intent)
            ControllerResult.Success(Unit, permissionStatus = permissionStatus())
        }.getOrElse { e ->
            ControllerResult.Failure(
                message = e.message ?: "Failed to start calendar",
                error = errorCode,
                permissionStatus = permissionStatus()
            )
        }
    }

    private fun hasReadCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionStatus(): String {
        return if (hasReadCalendarPermission()) "READ_CALENDAR_GRANTED" else "READ_CALENDAR_DENIED"
    }
}
