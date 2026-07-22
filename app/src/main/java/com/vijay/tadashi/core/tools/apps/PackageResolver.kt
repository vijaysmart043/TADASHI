package com.vijay.tadashi.core.tools.apps

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageResolver @Inject constructor(
    private val repository: InstalledAppsRepository
) {
    fun resolveApp(query: String): InstalledApp? {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return null

        if (looksLikePackageName(trimmed)) {
            return repository.findByPackage(trimmed)
        }

        return repository.findByName(trimmed)
            ?: repository.search(trimmed, limit = 1).firstOrNull()
    }

    private fun looksLikePackageName(value: String): Boolean {
        if (!value.contains('.')) return false
        if (value.contains(' ')) return false
        return true
    }
}

