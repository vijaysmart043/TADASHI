package com.vijay.tadashi.core.tools.apps

interface InstalledAppsRepository {
    fun findByName(appName: String): InstalledApp?
    fun findByPackage(packageName: String): InstalledApp?
    fun search(query: String, limit: Int = 10): List<InstalledApp>
}

