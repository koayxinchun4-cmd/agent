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

        assertEquals("工具 risky 需要明确用户确认后才能执行", result?.message)
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

    private class TestTool(
        override val id: String,
        override val riskLevel: RiskLevel
    ) : AgentTool {
        override val name: String = id
        override val description: String = "test tool"
        override suspend fun execute(task: AgentTask): ToolResult = ToolResult.Success("ok")
    }
}
