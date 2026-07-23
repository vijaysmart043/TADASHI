package com.vijay.tadashi.core.tools.alarmtimer

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import com.vijay.tadashi.core.tools.common.ControllerResult
import com.vijay.tadashi.core.tools.common.ToolExecutionReportDecoder
import com.vijay.tadashi.core.tools.system.ToolExecutionReports
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmTimerTool @Inject constructor(
    private val controller: AlarmTimerController
) : Tool {
    override val id: String = "ALARM_TIMER"
    override val displayName: String = "Alarm & Timer"
    override val description: String =
        "Creates/cancels alarms and timers. Actions: SET_ALARM, CANCEL_ALARM, SET_TIMER, CANCEL_TIMER. Accepts time strings like '7 AM' or duration like '10 minutes'."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.ALARMS

    override suspend fun execute(request: ToolRequest): ToolResult {
        val startMs = System.currentTimeMillis()
        val action = inferAction(request.arguments["action"]?.trim())
        val label = request.arguments["label"]?.trim()
            ?: request.arguments["message"]?.trim()

        ToolsLogger.d("AlarmTimer tool: action=$action label=$label args=${request.arguments}")

        val result = when (action) {
            "SET_ALARM" -> setAlarm(request, label, startMs)
            "CANCEL_ALARM" -> exec(startMs, "Cancelling alarm") { controller.cancelAlarm(label) }
            "SET_TIMER" -> setTimer(request, label, startMs)
            "CANCEL_TIMER" -> exec(startMs, "Cancelling timer") { controller.cancelTimer(label) }
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
            "AlarmTimer tool finished: action=$action timeMs=$elapsed verification=${report?.verification} permissionStatus=${report?.error ?: "N/A"} payload=$payload"
        )
        return result
    }

    private fun setAlarm(request: ToolRequest, label: String?, startMs: Long): ToolResult {
        val time = parseTime(
            hourRaw = request.arguments["hour"] ?: request.arguments["hours"],
            minuteRaw = request.arguments["minute"] ?: request.arguments["minutes"],
            timeRaw = request.arguments["time"] ?: request.arguments["at"]
        ) ?: return ToolExecutionReports.failure(
            message = "Missing or invalid alarm time",
            verification = false,
            executionTimeMs = System.currentTimeMillis() - startMs,
            error = "INVALID_TIME"
        )

        val (hour, minute) = time
        val msg = "Setting alarm for %02d:%02d".format(hour, minute)
        return exec(startMs, msg) { controller.setAlarm(hour, minute, label) }
    }

    private fun setTimer(request: ToolRequest, label: String?, startMs: Long): ToolResult {
        val lengthSeconds = parseDurationSeconds(
            secondsRaw = request.arguments["seconds"],
            minutesRaw = request.arguments["minutes"],
            durationRaw = request.arguments["duration"] ?: request.arguments["for"]
        ) ?: return ToolExecutionReports.failure(
            message = "Missing or invalid timer duration",
            verification = false,
            executionTimeMs = System.currentTimeMillis() - startMs,
            error = "INVALID_DURATION"
        )

        val minutes = lengthSeconds / 60
        val seconds = lengthSeconds % 60
        val msg = if (minutes > 0 && seconds > 0) {
            "Setting timer for ${minutes}m ${seconds}s"
        } else if (minutes > 0) {
            "Setting timer for ${minutes} minutes"
        } else {
            "Setting timer for ${seconds} seconds"
        }
        return exec(startMs, msg) { controller.setTimer(lengthSeconds, label) }
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
                error = r.error ?: "ALARM_TIMER_FAILED"
            )
        }
    }

    private fun inferAction(actionRaw: String?): String {
        val a = actionRaw?.trim()?.uppercase().orEmpty()
        return when (a) {
            "", "ALARM" -> "SET_ALARM"
            "TIMER" -> "SET_TIMER"
            "CREATE_ALARM", "SET_ALARM" -> "SET_ALARM"
            "CANCEL_ALARM", "DISMISS_ALARM", "REMOVE_ALARM" -> "CANCEL_ALARM"
            "CREATE_TIMER", "SET_TIMER" -> "SET_TIMER"
            "CANCEL_TIMER", "DISMISS_TIMER", "REMOVE_TIMER" -> "CANCEL_TIMER"
            else -> a
        }
    }

    private fun parseTime(
        hourRaw: String?,
        minuteRaw: String?,
        timeRaw: String?
    ): Pair<Int, Int>? {
        val hour = hourRaw?.trim()?.toIntOrNull()
        val minute = minuteRaw?.trim()?.toIntOrNull()
        if (hour != null) {
            val m = minute ?: 0
            if (hour in 0..23 && m in 0..59) return hour to m
        }

        val t = timeRaw?.trim()?.lowercase().orEmpty()
        if (t.isBlank()) return null

        val match = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""").find(t) ?: return null
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
        return h to m
    }

    private fun parseDurationSeconds(
        secondsRaw: String?,
        minutesRaw: String?,
        durationRaw: String?
    ): Int? {
        val seconds = secondsRaw?.trim()?.toIntOrNull()
        val minutes = minutesRaw?.trim()?.toIntOrNull()
        if (minutes != null) return (minutes.coerceAtLeast(0) * 60) + (seconds ?: 0).coerceAtLeast(0)
        if (seconds != null) return seconds.coerceAtLeast(0)

        val d = durationRaw?.trim()?.lowercase().orEmpty()
        if (d.isBlank()) return null

        var total = 0
        Regex("""(\d+)\s*(h|hr|hour|hours|m|min|minute|minutes|s|sec|second|seconds)""")
            .findAll(d)
            .forEach { m0 ->
                val value = m0.groupValues[1].toIntOrNull() ?: return@forEach
                val unit = m0.groupValues[2]
                total += when (unit) {
                    "h", "hr", "hour", "hours" -> value * 3600
                    "m", "min", "minute", "minutes" -> value * 60
                    else -> value
                }
            }
        return total.takeIf { it > 0 }
    }
}
