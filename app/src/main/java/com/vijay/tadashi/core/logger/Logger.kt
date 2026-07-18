package com.vijay.tadashi.core.logger

import android.util.Log
import com.vijay.tadashi.core.constants.Constants

object Logger {
    fun d(message: String, tag: String = Constants.TAG) {
        Log.d(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = Constants.TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun i(message: String, tag: String = Constants.TAG) {
        Log.i(tag, message)
    }

    fun w(message: String, tag: String = Constants.TAG) {
        Log.w(tag, message)
    }

    fun v(message: String, tag: String = Constants.TAG) {
        Log.v(tag, message)
    }
}