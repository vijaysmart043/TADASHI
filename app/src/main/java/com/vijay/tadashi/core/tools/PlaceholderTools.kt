package com.vijay.tadashi.core.tools

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeTool @Inject constructor() : Tool {
    override val id: String = "VOLUME"
    override val displayName: String = "Volume"
    override val description: String = "Adjusts the device media volume."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.MEDIA

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}

@Singleton
class BrightnessTool @Inject constructor() : Tool {
    override val id: String = "BRIGHTNESS"
    override val displayName: String = "Brightness"
    override val description: String = "Adjusts the device screen brightness."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.DEVICE

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}

@Singleton
class CameraTool @Inject constructor() : Tool {
    override val id: String = "CAMERA"
    override val displayName: String = "Camera"
    override val description: String = "Opens the camera."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.CAMERA

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}

@Singleton
class CallTool @Inject constructor() : Tool {
    override val id: String = "CALL"
    override val displayName: String = "Call"
    override val description: String = "Places a phone call."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.PHONE

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}

@Singleton
class AlarmTool @Inject constructor() : Tool {
    override val id: String = "ALARM"
    override val displayName: String = "Alarm"
    override val description: String = "Creates or modifies alarms."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.ALARMS

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}

@Singleton
class ClipboardTool @Inject constructor() : Tool {
    override val id: String = "CLIPBOARD"
    override val displayName: String = "Clipboard"
    override val description: String = "Reads or writes the clipboard."
    override val requiredPermissions: List<ToolPermission> = emptyList()
    override val category: ToolCategory = ToolCategory.CLIPBOARD

    override suspend fun execute(request: ToolRequest): ToolResult = ToolResult.Success("Not implemented yet.")
}
