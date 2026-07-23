package com.vijay.tadashi.core.tools.alarmtimer

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.vijay.tadashi.core.tools.common.ControllerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAlarmTimerController @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmTimerController {
    override fun setAlarm(hour: Int, minute: Int, label: String?): ControllerResult<Unit> {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        return start(intent, "SET_ALARM_FAILED")
    }

    override fun cancelAlarm(label: String?): ControllerResult<Unit> {
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
            label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
        }
        return start(intent, "CANCEL_ALARM_FAILED")
    }

    override fun setTimer(lengthSeconds: Int, label: String?): ControllerResult<Unit> {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_LENGTH, lengthSeconds)
            label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        return start(intent, "SET_TIMER_FAILED")
    }

    override fun cancelTimer(label: String?): ControllerResult<Unit> {
        val intent = Intent(AlarmClock.ACTION_DISMISS_TIMER).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
            label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
        }
        return start(intent, "CANCEL_TIMER_FAILED")
    }

    private fun start(intent: Intent, errorCode: String): ControllerResult<Unit> {
        val resolved = intent.resolveActivity(context.packageManager) != null
        if (!resolved) {
            return ControllerResult.Failure(
                message = "No application can handle this request",
                error = "NO_HANDLER",
                permissionStatus = "N/A"
            )
        }
        return runCatching {
            context.startActivity(intent)
            ControllerResult.Success(Unit, permissionStatus = "N/A")
        }.getOrElse { e ->
            ControllerResult.Failure(
                message = e.message ?: "Failed to start alarm/timer",
                error = errorCode,
                permissionStatus = "N/A"
            )
        }
    }
}

