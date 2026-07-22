package com.vijay.tadashi.core.ai.planner

import android.util.Log

object PlannerLogger {
    private const val TAG = "TADASHI-PLANNER"

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
