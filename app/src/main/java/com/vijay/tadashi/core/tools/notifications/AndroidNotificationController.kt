package com.vijay.tadashi.core.tools.notifications

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import com.vijay.tadashi.core.tools.common.ControllerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificationController @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationController {
    override fun isNotificationAccessEnabled(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    override fun readActiveNotifications(
        packageOrAppFilter: String?
    ): ControllerResult<List<ActiveNotification>> {
        if (!isNotificationAccessEnabled()) {
            return ControllerResult.PermissionDenied(
                permissionStatus = "NOTIFICATION_ACCESS_DENIED",
                message = "Notification access is not enabled"
            )
        }

        val filter = packageOrAppFilter?.trim().orEmpty().ifBlank { null }
        val all = NotificationSnapshotStore.snapshot()
        val filtered = filter?.let { f ->
            all.filter { matchesFilter(it, f) }
        } ?: all

        return ControllerResult.Success(
            value = filtered,
            permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
        )
    }

    override fun clearDismissibleNotifications(
        packageOrAppFilter: String?
    ): ControllerResult<Int> {
        if (!isNotificationAccessEnabled()) {
            return ControllerResult.PermissionDenied(
                permissionStatus = "NOTIFICATION_ACCESS_DENIED",
                message = "Notification access is not enabled"
            )
        }

        if (!NotificationListenerBridge.isConnected()) {
            return ControllerResult.Failure(
                message = "Notification listener not connected",
                error = "LISTENER_NOT_CONNECTED",
                permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
            )
        }

        val filter = packageOrAppFilter?.trim().orEmpty().ifBlank { null }
        val candidates = NotificationSnapshotStore.snapshot()
            .filter { it.isClearable }
            .let { list ->
                filter?.let { f -> list.filter { matchesFilter(it, f) } } ?: list
            }

        var cleared = 0
        candidates.forEach { n ->
            val ok = NotificationListenerBridge.cancelNotification(n.key)
            if (ok) {
                cleared += 1
                NotificationSnapshotStore.remove(n.key)
            }
        }

        return ControllerResult.Success(
            value = cleared,
            permissionStatus = "NOTIFICATION_ACCESS_GRANTED"
        )
    }

    private fun matchesFilter(n: ActiveNotification, filter: String): Boolean {
        val f = filter.lowercase()
        if (n.packageName.lowercase().contains(f)) return true

        val label = runCatching { appLabelForPackage(n.packageName) }.getOrNull()
        return label?.lowercase()?.contains(f) == true
    }

    private fun appLabelForPackage(packageName: String): String? {
        val pm = context.packageManager
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info)?.toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
