package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.AgentTool
import com.example.agent.nexus.tool.RiskLevel
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NexusAgentConfirmationTest {
    private fun agent(): NexusAgent {
        val tool = object : AgentTool {
            override val id = "app_agent"
            override val name = "Risky Tool"
            override val description = "A tool that changes something"
            override val riskLevel = RiskLevel.REQUIRES_CONFIRMATION
            override suspend fun execute(task: AgentTask): ToolResult = ToolResult.Success("changed")
        }
        return NexusAgent(ToolRegistry(listOf(tool)))
    }

    @Test
    fun preview_requests_confirmation_before_execution() {
        val request = agent().previewConfirmation(AgentTask("confirmation-1", "open app"))
        assertNotNull(request)
        assertEquals("app_agent", request?.tool?.id)
    }

    @Test
    fun confirmed_task_skips_confirmation_preflight() {
        val task = AgentTask("confirmation-2", "open app", mapOf(AgentTask.CONFIRMATION_GRANTED to "true"))
        assertNull(agent().previewConfirmation(task))
    }
}
