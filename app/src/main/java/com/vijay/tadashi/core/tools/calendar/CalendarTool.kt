package com.vijay.tadashi.core.tools.calendar

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import com.vijay.tadashi.core.tools.common.ControllerResult
import com.vijay.tadashi.core.tools.common.ToolExecutionReportDecoder
import com.vijay.tadashi.core.tools.system.ToolExecutionReports
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarTool @Inject constructor(
    private val controller: CalendarController
) : Tool {
    override val id: String = "CALENDAR"
    override val displayName: String = "Calendar"
    override val description: String =
        "Opens calendar, creates events, and reads today's events. Actions: OPEN, CREATE_EVENT, TODAY_EVENTS."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.OTHER

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = request.arguments["action"]?.trim()?.uppercase() ?: "OPEN"

        ToolsLogger.d("Calendar tool: action=$action args=${request.arguments}")

        val result = when (action) {
            "OPEN" -> exec(startMs, "Opening calendar") { controller.openCalendar() }
            "CREATE_EVENT", "CREATE", "EVENT" -> createEvent(request, startMs)
            "TODAY_EVENTS", "TODAY" -> todayEvents(startMs)
            else -> ToolExecutionReports.failure(
                message = "Unsupported action: $action",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "UNSUPPORTED_ACTION"
            )
        }

        val elapsed = System.currentTimeMillis() - startMs
        val payload = when (result) {
            is ToolResult.Success -> result.message
            is ToolResult.Failure -> result.message
            is ToolResult.Unsupported -> result.message
            is ToolResult.Cancelled -> result.message
            is ToolResult.PermissionDenied -> null
        }
        val report = ToolExecutionReportDecoder.decode(result)
        ToolsLogger.d(
            "Calendar tool finished: action=$action timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
        )
        return result
    }

    private fun createEvent(request: ToolRequest, startMs: Long): ToolResult {
        val title = request.arguments["title"]?.trim()
            ?: request.arguments["name"]?.trim()
            ?: return ToolExecutionReports.failure(
                message = "Missing event title",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "MISSING_TITLE"
            )

        val start = parseTimeMs(
            rawMs = request.arguments["startTimeMs"] ?: request.arguments["startMs"],
            dateRaw = request.arguments["date"],
            timeRaw = request.arguments["startTime"] ?: request.arguments["time"]
        ) ?: return ToolExecutionReports.failure(
            message = "Missing or invalid start time",
            verification = false,
            executionTimeMs = System.currentTimeMillis() - startMs,
            error = "INVALID_START_TIME"
        )

        val end = parseTimeMs(
            rawMs = request.arguments["endTimeMs"] ?: request.arguments["endMs"],
            dateRaw = request.arguments["date"],
            timeRaw = request.arguments["endTime"]
        ) ?: (start + 60 * 60 * 1000L)

        if (end < start) {
            return ToolExecutionReports.failure(
                message = "End time must be after start time",
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = "INVALID_END_TIME"
            )
        }

        val draft = CalendarEventDraft(
            title = title,
            startTimeMs = start,
            endTimeMs = end,
            location = request.arguments["location"]?.trim(),
            description = request.arguments["description"]?.trim()
        )

        val msg = "Creating calendar event: $title"
        return exec(startMs, msg) { controller.createEvent(draft) }
    }

    private fun todayEvents(startMs: Long): ToolResult {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        return when (val r = controller.getTodayEvents(start, end)) {
            is ControllerResult.Success -> {
                val list = r.value
                val message = if (list.isEmpty()) {
                    "No events today"
                } else {
                    buildString {
                        append("Today's events (${list.size})")
                        list.take(6).forEach { e ->
                            append("\n- ${e.title ?: "Event"}")
                        }
                        if (list.size > 6) append("\n(and ${list.size - 6} more)")
                    }
                }
                ToolExecutionReports.success(
                    message = message,
                    verification = true,
                    executionTimeMs = System.currentTimeMillis() - startMs
                )
            }
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "TODAY_EVENTS_FAILED"
            )
        }
    }

    private fun exec(
        startMs: Long,
        message: String,
        block: () -> ControllerResult<Unit>
    ): ToolResult {
        return when (val r = block()) {
            is ControllerResult.Success -> ToolExecutionReports.success(
                message = message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs
            )
            is ControllerResult.PermissionDenied -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.permissionStatus
            )
            is ControllerResult.Failure -> ToolExecutionReports.failure(
                message = r.message,
                verification = false,
                executionTimeMs = System.currentTimeMillis() - startMs,
                error = r.error ?: "CALENDAR_FAILED"
            )
        }
    }

    private fun parseTimeMs(rawMs: String?, dateRaw: String?, timeRaw: String?): Long? {
        rawMs?.trim()?.toLongOrNull()?.let { return it }

        val zone = ZoneId.systemDefault()
        val date = parseDate(dateRaw) ?: LocalDate.now(zone)
        val time = parseTime(timeRaw) ?: return null
        val zdt = ZonedDateTime.of(date, time, zone)
        return zdt.toInstant().toEpochMilli()
    }

    private fun parseDate(raw: String?): LocalDate? {
        val r = raw?.trim()?.lowercase().orEmpty()
        if (r.isBlank() || r == "today") return null
        return runCatching { LocalDate.parse(r) }.getOrNull()
    }

    private fun parseTime(raw: String?): LocalTime? {
        val r = raw?.trim()?.lowercase().orEmpty()
        if (r.isBlank()) return null

        runCatching { LocalTime.parse(r) }
            .getOrNull()
            ?.let { return it }

        val match = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""").find(r) ?: return null
        var h = match.groupValues[1].toIntOrNull() ?: return null
        val m = match.groupValues[2].takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
        val ap = match.groupValues[3]
        if (m !in 0..59) return null
        if (ap == "am" || ap == "pm") {
            if (h !in 1..12) return null
            if (ap == "pm" && h != 12) h += 12
            if (ap == "am" && h == 12) h = 0
        }
        if (h !in 0..23) return null
        return LocalTime.of(h, m)
    }
}
