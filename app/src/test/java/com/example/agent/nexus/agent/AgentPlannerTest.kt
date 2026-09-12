package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentPlannerTest {
    private val planner = AgentPlanner()

    @Test
    fun `routes explicit app launch request to app agent`() {
        val plan = planner.plan(
            AgentTask("1", "open app package:com.example.notes"),
            setOf("app_agent", "local_task")
        )

        assertEquals("app_agent", plan.toolId)
        assertEquals("use_tool:app_agent", plan.steps[1])
    }

    @Test
    fun `does not route app request when app agent is unavailable`() {
        val plan = planner.plan(
            AgentTask("2", "open app package:com.example.notes"),
            setOf("local_task")
        )

        assertEquals("local_task", plan.toolId)
    }

    @Test
    fun `routes traditional chinese research intent to web research`() {
        val plan = planner.plan(
            AgentTask("3", "搜尋這個主題的資料"),
            setOf("web_research", "local_task")
        )

        assertEquals("web_research", plan.toolId)
    }

    @Test
    fun `routes traditional chinese file intent to file agent`() {
        val plan = planner.plan(
            AgentTask("4", "讀取這個檔案"),
            setOf("file_agent", "local_task")
        )

        assertEquals("file_agent", plan.toolId)
    }

    @Test
    fun `exposes ordered subtasks for a multi-step goal`() {
        val plan = planner.plan(
            AgentTask("5", "搜尋 repo then 檢查 CI then 整理結果"),
            setOf("web_research", "local_task")
        )

        assertEquals(
            listOf("搜尋 repo", "檢查 CI", "整理結果"),
            plan.subtasks
        )
        assertTrue(plan.steps[1].startsWith("subtask:1:"))
        assertTrue(plan.steps[2].startsWith("subtask:2:"))
        assertTrue(plan.steps[3].startsWith("subtask:3:"))
    }
}
