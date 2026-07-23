package com.vijay.tadashi.core.tools.calendar

import com.vijay.tadashi.core.tools.common.ControllerResult

interface CalendarController {
    fun openCalendar(): ControllerResult<Unit>
    fun createEvent(draft: CalendarEventDraft): ControllerResult<Unit>
    fun getTodayEvents(startOfDayMs: Long, endOfDayMs: Long): ControllerResult<List<CalendarEventSummary>>
}

