package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToolPermissionTest {
    @Test
    fun allows_safe_tool_without_confirmation() {
        val tool = TestTool("safe", RiskLevel.SAFE)

        assertNull(ToolPermission.check(tool, AgentTask("task-1", "test")))
    }

    @Test
    fun blocks_risky_tool_without_confirmation() {
        val tool = TestTool("risky", RiskLevel.REQUIRES_CONFIRMATION)

        val result = ToolPermission.check(tool, AgentTask("task-1", "test"))

        assertEquals("Tool risky requires explicit user confirmation", result?.message)
    }

    @Test
    fun allows_risky_tool_with_explicit_confirmation() {
        val tool = TestTool("risky", RiskLevel.REQUIRES_CONFIRMATION)
        val task = AgentTask(
            id = "task-1",
            input = "test",
            metadata = mapOf(AgentTask.CONFIRMATION_GRANTED to "true")
        )

        assertNull(ToolPermission.check(tool, task))
    }

    @Test
    fun blocks_tool_when_android_permission_is_missing() {
        val tool = TestTool(
            id = "camera",
            riskLevel = RiskLevel.SAFE,
            requiredPermissions = setOf("android.permission.CAMERA")
        )

        val result = ToolPermission.check(tool, AgentTask("task-1", "test"))

        assertEquals(
            "Tool camera requires Android permission(s): android.permission.CAMERA. Grant the permission before retrying.",
            result?.message
        )
    }

    @Test
    fun allows_tool_when_required_android_permission_is_granted() {
        val tool = TestTool(
            id = "camera",
            riskLevel = RiskLevel.SAFE,
            requiredPermissions = setOf("android.permission.CAMERA")
        )
        val task = AgentTask(
            id = "task-1",
            input = "test",
            metadata = mapOf(AgentTask.GRANTED_PERMISSIONS to "android.permission.CAMERA")
        )

        assertNull(ToolPermission.check(tool, task))
    }

    @Test
    fun blocks_only_the_missing_permission() {
        val tool = TestTool(
            id = "media",
            riskLevel = RiskLevel.SAFE,
            requiredPermissions = setOf(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"
            )
        )
        val task = AgentTask(
            id = "task-1",
            input = "test",
            metadata = mapOf(AgentTask.GRANTED_PERMISSIONS to "android.permission.CAMERA")
        )

        val result = ToolPermission.check(tool, task)

        assertEquals(
            "Tool media requires Android permission(s): android.permission.RECORD_AUDIO. Grant the permission before retrying.",
            result?.message
        )
    }

    private class TestTool(
        override val id: String,
        override val riskLevel: RiskLevel,
        override val requiredPermissions: Set<String> = emptySet()
    ) : AgentTool {
        override val name: String = id
        override val description: String = "test tool"
        override suspend fun execute(task: AgentTask): ToolResult = ToolResult.Success("ok")
    }
}
