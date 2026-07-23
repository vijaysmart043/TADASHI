package com.vijay.tadashi.core.tools.calendar

data class CalendarEventDraft(
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val location: String? = null,
    val description: String? = null
)

data class CalendarEventSummary(
    val title: String?,
    val startTimeMs: Long,
    val endTimeMs: Long
)

