package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentPlannerPhase5Test {
    private val tools = setOf("web_research", "office", "github", "file_agent", "local_task")

    @Test
    fun routesWebResearch() {
        assertEquals("web_research", AgentPlanner().plan(AgentTask("1", "web research https://example.com"), tools).toolId)
    }

    @Test
    fun routesOffice() {
        assertEquals("office", AgentPlanner().plan(AgentTask("2", "office spreadsheet report"), tools).toolId)
    }

    @Test
    fun routesGitHub() {
        assertEquals("github", AgentPlanner().plan(AgentTask("3", "check github koayxinchun4-cmd/agent"), tools).toolId)
    }

    @Test
    fun keepsGenericFileTaskOnFileAgent() {
        assertEquals("file_agent", AgentPlanner().plan(AgentTask("4", "读取文件 note.txt"), tools).toolId)
    }
}
