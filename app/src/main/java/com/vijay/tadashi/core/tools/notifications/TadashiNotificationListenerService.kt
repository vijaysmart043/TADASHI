package com.vijay.tadashi.core.tools.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class TadashiNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationListenerBridge.setService(this)
        val snapshot = activeNotifications?.toList().orEmpty()
        snapshot.forEach { sbn ->
            NotificationSnapshotStore.upsert(sbn.toActiveNotification())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationSnapshotStore.upsert(sbn.toActiveNotification())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationSnapshotStore.remove(sbn.key)
    }

    override fun onDestroy() {
        NotificationListenerBridge.clearService(this)
        super.onDestroy()
    }

    private fun StatusBarNotification.toActiveNotification(): ActiveNotification {
        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        return ActiveNotification(
            key = key,
            packageName = packageName,
            title = title,
            text = text,
            postTimeMs = postTime,
            isClearable = isClearable
        )
    }
}

