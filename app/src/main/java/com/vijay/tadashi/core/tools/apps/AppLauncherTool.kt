package com.vijay.tadashi.core.tools.apps

import com.vijay.tadashi.core.tools.Tool
import com.vijay.tadashi.core.tools.ToolCategory
import com.vijay.tadashi.core.tools.ToolPermission
import com.vijay.tadashi.core.tools.ToolRequest
import com.vijay.tadashi.core.tools.ToolResult
import com.vijay.tadashi.core.tools.ToolsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLauncherTool @Inject constructor(
    private val repository: InstalledAppsRepository,
    private val packageResolver: PackageResolver,
    private val appLauncher: AppLauncher
) : Tool {
    override val id: String = "OPEN_APP"
    override val displayName: String = "Open App"
    override val description: String = "Launches an installed app."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.APPS

    override suspend fun execute(request: ToolRequest): ToolResult {
        val requestedApp = request.arguments["app"]
        val requestedPackage = request.arguments["package"] ?: request.arguments["packageName"]
        val requested = requestedApp ?: requestedPackage
            ?: return ToolResult.Failure("Missing argument: app")

        ToolsLogger.d("Requested app: $requested")
        val resolved = if (!requestedPackage.isNullOrBlank()) {
            repository.findByPackage(requestedPackage)
        } else {
            packageResolver.resolveApp(requested)
        }
            ?: return ToolResult.Failure("Unknown app: $requested")

        ToolsLogger.d("Resolved package: ${resolved.packageName}")
        val result = appLauncher.launch(resolved.packageName)
        return when (result) {
            is ToolResult.Success -> {
                ToolsLogger.d("Launch success: ${resolved.packageName}")
                ToolResult.Success("Opened ${resolved.displayName}")
            }

            is ToolResult.Failure -> {
                ToolsLogger.d("Launch failure: ${resolved.packageName}")
                result
            }

            else -> {
                ToolsLogger.d("Launch failure: ${resolved.packageName}")
                ToolResult.Failure("Launch failed for package: ${resolved.packageName}")
            }
        }
    }
}
