package com.vijay.tadashi.core.tools.ringer

enum class RingerMode {
    SILENT,
    VIBRATE,
    NORMAL
}

data class RingerStatusResult(
    val mode: RingerMode?,
    val message: String,
    val error: String? = null
)

data class RingerChangeResult(
    val verification: Boolean,
    val mode: RingerMode?,
    val requiresUserAction: Boolean,
    val message: String,
    val error: String? = null
)

interface RingerController {
    fun status(): RingerStatusResult
    fun setMode(mode: RingerMode): RingerChangeResult
}

