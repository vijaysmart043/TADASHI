package com.vijay.tadashi.core.tools.camera

import com.vijay.tadashi.core.tools.common.ControllerResult

interface CameraController {
    fun openCamera(lensFacing: String? = null): ControllerResult<Unit>
    fun openVideoMode(lensFacing: String? = null): ControllerResult<Unit>
    fun takePicture(lensFacing: String? = null): ControllerResult<Unit>
    fun switchCamera(lensFacing: String?): ControllerResult<Unit>
}

