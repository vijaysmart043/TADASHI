package com.vijay.tadashi.core.tools.notifications

data class ActiveNotification(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postTimeMs: Long,
    val isClearable: Boolean
)

