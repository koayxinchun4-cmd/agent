package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `does not silently use local task when requested app agent is unavailable`() {
        val plan = planner.plan(
            AgentTask("2", "open app package:com.example.notes"),
            setOf("local_task")
        )

        assertNull(plan.toolId)
    }

    @Test
    fun `ordinary task remains a direct model request`() {
        val plan = planner.plan(
            AgentTask("3", "explain how Nexus works"),
            setOf("local_task", "memory", "skills")
        )

        assertNull(plan.toolId)
        assertEquals(listOf("understand_request", "answer"), plan.steps)
    }

    @Test
    fun `explicit local task still uses local analyzer`() {
        val plan = planner.plan(
            AgentTask("4", "local task: summarize this request"),
            setOf("local_task")
        )

        assertEquals("local_task", plan.toolId)
    }
}
