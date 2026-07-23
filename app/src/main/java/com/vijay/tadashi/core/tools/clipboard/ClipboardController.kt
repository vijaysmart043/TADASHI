package com.vijay.tadashi.core.tools.clipboard

data class ClipboardReadResult(
    val text: String?,
    val restricted: Boolean,
    val message: String,
    val error: String? = null
)

interface ClipboardController {
    fun copy(text: String): Result<Unit>
    fun read(): ClipboardReadResult
    fun clear(): Result<Unit>
    fun share(text: String): Result<Unit>
}

