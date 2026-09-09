package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
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
}
