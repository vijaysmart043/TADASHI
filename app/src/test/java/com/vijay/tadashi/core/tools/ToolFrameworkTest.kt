package com.vijay.tadashi.core.tools

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolFrameworkTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private class FakeTool : Tool {
        override val id: String = "TEST_TOOL"
        override val displayName: String = "Test Tool"
        override val description: String = "Test tool."
        override val requiredPermissions: List<ToolPermission> = emptyList()
        override val category: ToolCategory = ToolCategory.OTHER

        override suspend fun execute(request: ToolRequest): ToolResult {
            return ToolResult.Success("OK")
        }
    }

    @Test
    fun registry_registersAndFindsTool() {
        val registry = ToolRegistry(
            tools = setOf(FakeTool())
        )

        val found = registry.find("TEST_TOOL")
        assertNotNull(found)
        assertEquals("TEST_TOOL", found?.id)
        assertEquals(1, registry.getAll().size)
    }

    @Test
    fun parser_parsesStructuredToolJson() {
        val parser = ToolParser(json)
        val request = parser.parse(
            structuredOutput = """{"version":"1.0","tool":"OPEN_APP","arguments":{"app":"Chrome"}}""",
            caller = "AI",
            timestampMs = 123L
        )

        assertNotNull(request)
        assertEquals("OPEN_APP", request?.toolId)
        assertEquals("Chrome", request?.arguments?.get("app"))
        assertEquals("AI", request?.caller)
        assertEquals(123L, request?.timestampMs)
    }

    @Test
    fun executor_executesRegisteredTool() = runBlocking {
        val registry = ToolRegistry(
            tools = setOf(FakeTool())
        )
        val executor = ToolExecutor(
            registry = registry,
            permissionManager = DefaultToolPermissionManager()
        )

        val result = executor.execute(
            ToolRequest(
                toolId = "TEST_TOOL",
                arguments = emptyMap(),
                caller = "AI",
                timestampMs = 123L
            )
        )

        assertTrue(result is ToolResult.Success)
        assertEquals("OK", (result as ToolResult.Success).message)
    }
}
