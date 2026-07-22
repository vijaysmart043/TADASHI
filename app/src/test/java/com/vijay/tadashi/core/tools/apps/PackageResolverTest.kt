package com.vijay.tadashi.core.tools.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackageResolverTest {
    private class FakeInstalledAppsRepository(
        private val apps: List<InstalledApp>
    ) : InstalledAppsRepository {
        override fun findByName(appName: String): InstalledApp? {
            val q = appName.trim().lowercase()
            return apps.firstOrNull { it.displayName.trim().lowercase() == q }
        }

        override fun findByPackage(packageName: String): InstalledApp? {
            val q = packageName.trim().lowercase()
            return apps.firstOrNull { it.packageName.trim().lowercase() == q }
        }

        override fun search(query: String, limit: Int): List<InstalledApp> {
            val q = query.trim().lowercase()
            return apps.filter {
                it.displayName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }.take(limit)
        }
    }

    private val resolver = PackageResolver(
        repository = FakeInstalledAppsRepository(
            apps = listOf(
                InstalledApp(packageName = "com.android.chrome", displayName = "Chrome"),
                InstalledApp(packageName = "com.whatsapp", displayName = "WhatsApp"),
                InstalledApp(packageName = "com.instagram.android", displayName = "Instagram"),
                InstalledApp(packageName = "com.android.camera", displayName = "Camera")
            )
        )
    )

    @Test
    fun resolveApp_exactName() {
        val resolved = resolver.resolveApp("Chrome")
        assertEquals("com.android.chrome", resolved?.packageName)
    }

    @Test
    fun resolveApp_partialName() {
        val resolved = resolver.resolveApp("insta")
        assertEquals("com.instagram.android", resolved?.packageName)
    }

    @Test
    fun resolveApp_packageName() {
        val resolved = resolver.resolveApp("com.whatsapp")
        assertEquals("com.whatsapp", resolved?.packageName)
    }

    @Test
    fun resolveApp_unknown() {
        val resolved = resolver.resolveApp("does-not-exist")
        assertNull(resolved)
    }
}

