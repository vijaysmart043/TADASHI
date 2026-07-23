package com.vijay.tadashi.core.tools.notifications

import java.lang.ref.WeakReference

object NotificationListenerBridge {
    @Volatile
    private var serviceRef: WeakReference<TadashiNotificationListenerService>? = null

    fun setService(service: TadashiNotificationListenerService?) {
        serviceRef = service?.let { WeakReference(it) }
    }

    fun clearService(service: TadashiNotificationListenerService) {
        val current = serviceRef?.get() ?: return
        if (current === service) {
            serviceRef = null
        }
    }

    fun isConnected(): Boolean {
        return serviceRef?.get() != null
    }

    fun cancelNotification(key: String): Boolean {
        val service = serviceRef?.get() ?: return false
        service.cancelNotification(key)
        return true
    }
}
