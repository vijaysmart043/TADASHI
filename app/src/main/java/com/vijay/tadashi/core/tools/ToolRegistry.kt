package com.vijay.tadashi.core.tools

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    tools: Set<@JvmSuppressWildcards Tool>
) {
    private val toolsById = ConcurrentHashMap<String, Tool>()

    init {
        tools.forEach { register(it) }
    }

    fun register(tool: Tool) {
        toolsById[tool.id] = tool
        ToolsLogger.d("Tool registered: ${tool.id}")
    }

    fun unregister(toolId: String) {
        val removed = toolsById.remove(toolId)
        if (removed != null) {
            ToolsLogger.d("Tool unregistered: $toolId")
        }
    }

    fun find(toolId: String): Tool? = toolsById[toolId]

    fun getAll(): List<Tool> = toolsById.values.sortedBy { it.id }
}
