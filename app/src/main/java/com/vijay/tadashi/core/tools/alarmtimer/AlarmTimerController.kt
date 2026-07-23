package com.vijay.tadashi.core.tools.alarmtimer

import com.vijay.tadashi.core.tools.common.ControllerResult

interface AlarmTimerController {
    fun setAlarm(hour: Int, minute: Int, label: String? = null): ControllerResult<Unit>
    fun cancelAlarm(label: String? = null): ControllerResult<Unit>
    fun setTimer(lengthSeconds: Int, label: String? = null): ControllerResult<Unit>
    fun cancelTimer(label: String? = null): ControllerResult<Unit>
}

