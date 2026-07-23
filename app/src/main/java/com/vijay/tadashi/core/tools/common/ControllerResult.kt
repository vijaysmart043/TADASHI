package com.vijay.tadashi.core.tools.common

sealed interface ControllerResult<out T> {
    val permissionStatus: String?

    data class Success<T>(
        val value: T,
        override val permissionStatus: String? = null
    ) : ControllerResult<T>

    data class PermissionDenied(
        override val permissionStatus: String,
        val message: String
    ) : ControllerResult<Nothing>

    data class Failure(
        val message: String,
        val error: String? = null,
        override val permissionStatus: String? = null
    ) : ControllerResult<Nothing>
}

