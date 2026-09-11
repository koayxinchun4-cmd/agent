package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelRouterTest {
    private val task = AgentTask("task-1", "input")
    private val answerPlan = AgentPlan("task-1")
    private val toolPlan = AgentPlan("task-1", toolId = "github")

    @Test
    fun `tool tasks prefer openrouter when available`() {
        val router = ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = true))

        assertEquals(ModelRoute.OpenRouter, router.route(task, toolPlan))
    }

    @Test
    fun `complex tasks prefer openrouter then gemini`() {
        val router = ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = true))

        assertEquals(
            ModelRoute.OpenRouter,
            router.route(task.copy(input = "analyze this architecture"), answerPlan)
        )
        assertEquals(
            ModelRoute.Gemini,
            ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = false))
                .route(task.copy(input = "analyze this architecture"), answerPlan)
        )
    }

    @Test
    fun `simple tasks prefer gemini when available`() {
        val router = ModelRouter(ModelAvailability(geminiAvailable = true, openRouterAvailable = true))

        assertEquals(ModelRoute.Gemini, router.route(task.copy(input = "summarize this"), answerPlan))
    }

    @Test
    fun `router falls back to local when no cloud provider is available`() {
        val router = ModelRouter()

        assertEquals(ModelRoute.Local, router.route(task, answerPlan))
    }
}
