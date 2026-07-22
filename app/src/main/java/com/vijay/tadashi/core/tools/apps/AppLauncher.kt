package com.vijay.tadashi.core.tools.apps

import android.content.Context
import android.content.Intent
import com.vijay.tadashi.core.tools.ToolResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun launch(packageName: String): ToolResult {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return ToolResult.Failure("No launch intent for package: $packageName")

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            ToolResult.Success("Launched $packageName")
        }.getOrElse { e ->
            ToolResult.Failure("Launch failed for package: $packageName (${e::class.simpleName})")
        }
    }
}
