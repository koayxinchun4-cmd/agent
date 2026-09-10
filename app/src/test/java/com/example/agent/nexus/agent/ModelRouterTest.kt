package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelRouterTest {
    private val task = AgentTask("1", "test")

    @Test
    fun `direct task prefers gemini when available`() {
        val router = ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = true))
        val plan = AgentPlan("1", null, ModelRoute.Local, listOf("understand_request", "answer"))

        assertEquals(ModelRoute.Gemini, router.route(task, plan))
    }

    @Test
    fun `tool task prefers openrouter`() {
        val router = ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = true))
        val plan = AgentPlan("1", "file_agent", ModelRoute.Local, listOf("understand_request", "use_tool:file_agent", "answer"))

        assertEquals(ModelRoute.OpenRouter, router.route(task, plan))
    }

    @Test
    fun `falls back to local when no remote provider is available`() {
        val router = ModelRouter()
        val plan = AgentPlan("1", null, ModelRoute.Local, listOf("understand_request", "answer"))

        assertEquals(ModelRoute.Local, router.route(task, plan))
    }
}
