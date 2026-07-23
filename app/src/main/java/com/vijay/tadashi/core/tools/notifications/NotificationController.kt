package com.vijay.tadashi.core.tools.notifications

import com.vijay.tadashi.core.tools.common.ControllerResult

interface NotificationController {
    fun isNotificationAccessEnabled(): Boolean

    fun readActiveNotifications(
        packageOrAppFilter: String?
    ): ControllerResult<List<ActiveNotification>>

    fun clearDismissibleNotifications(
        packageOrAppFilter: String?
    ): ControllerResult<Int>
}

