package com.vijay.tadashi.core.tools.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultInstalledAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledAppsRepository {
    private val cache = AtomicReference<List<InstalledApp>?>(null)

    override fun findByName(appName: String): InstalledApp? {
        if (appName.isBlank()) return null
        val query = normalize(appName)
        val apps = getOrLoad()

        return apps.firstOrNull { normalize(it.displayName) == query }
            ?: apps.firstOrNull { normalize(it.displayName).startsWith(query) }
            ?: apps.firstOrNull { normalize(it.displayName).contains(query) }
    }

    override fun findByPackage(packageName: String): InstalledApp? {
        if (packageName.isBlank()) return null
        val query = normalize(packageName)
        val apps = getOrLoad()

        return apps.firstOrNull { normalize(it.packageName) == query }
            ?: apps.firstOrNull { normalize(it.packageName).contains(query) }
    }

    override fun search(query: String, limit: Int): List<InstalledApp> {
        if (query.isBlank()) return emptyList()
        val q = normalize(query)
        val apps = getOrLoad()

        return apps
            .map { app -> app to score(app, q) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<InstalledApp, Int>> { it.second }.thenBy { it.first.displayName })
            .take(limit.coerceAtLeast(1))
            .map { it.first }
    }

    private fun getOrLoad(): List<InstalledApp> {
        val existing = cache.get()
        if (existing != null) return existing

        val loaded = loadLaunchableApps(context.packageManager)
        cache.compareAndSet(null, loaded)
        return cache.get() ?: loaded
    }

    private fun loadLaunchableApps(packageManager: PackageManager): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val flags = PackageManager.MATCH_DEFAULT_ONLY
        val resolveInfos = packageManager.queryIntentActivities(intent, flags)

        val unique = LinkedHashMap<String, InstalledApp>()
        resolveInfos.forEach { info ->
            val packageName = info.activityInfo?.packageName ?: return@forEach
            if (unique.containsKey(packageName)) return@forEach
            val label = runCatching { info.loadLabel(packageManager)?.toString() }.getOrNull()
            unique[packageName] = InstalledApp(
                packageName = packageName,
                displayName = label?.takeIf { it.isNotBlank() } ?: packageName
            )
        }

        return unique.values.sortedBy { it.displayName }
    }

    private fun score(app: InstalledApp, query: String): Int {
        val name = normalize(app.displayName)
        val pkg = normalize(app.packageName)

        if (name == query) return 100
        if (name.startsWith(query)) return 80
        if (name.contains(query)) return 60
        if (pkg == query) return 50
        if (pkg.contains(query)) return 30

        return 0
    }

    private fun normalize(value: String): String {
        return value.trim().lowercase()
    }
}

