package com.vijay.tadashi.core.tools.gallery

import com.vijay.tadashi.core.tools.common.ControllerResult

interface GalleryController {
    fun openGallery(): ControllerResult<Unit>
    fun openLatestImage(): ControllerResult<Unit>
    fun shareLatestImage(): ControllerResult<Unit>
}

