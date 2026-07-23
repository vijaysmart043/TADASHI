package com.vijay.tadashi.core.tools.notifications

import java.util.concurrent.ConcurrentHashMap

object NotificationSnapshotStore {
    private val byKey = ConcurrentHashMap<String, ActiveNotification>()

    fun upsert(notification: ActiveNotification) {
        byKey[notification.key] = notification
    }

    fun remove(key: String) {
        byKey.remove(key)
    }

    fun snapshot(): List<ActiveNotification> {
        return byKey.values
            .sortedByDescending { it.postTimeMs }
    }
}

