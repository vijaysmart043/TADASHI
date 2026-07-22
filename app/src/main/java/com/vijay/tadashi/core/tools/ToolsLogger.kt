package com.vijay.tadashi.core.tools

import android.util.Log

object ToolsLogger {
    private const val TAG = "TADASHI-TOOLS"

    fun d(message: String, throwable: Throwable? = null) {
        runCatching {
            if (throwable != null) {
                Log.d(TAG, message, throwable)
            } else {
                Log.d(TAG, message)
            }
        }
    }
}

