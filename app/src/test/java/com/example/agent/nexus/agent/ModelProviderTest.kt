package com.example.agent.nexus.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelProviderTest {
    private val provider = LocalModelProvider()

    @Test
    fun `local provider is always available`() {
        assertTrue(provider.isAvailable)
        assertEquals(ModelRoute.Local, provider.route)
    }

    @Test
    fun `local provider returns actionable response`() = runBlocking {
        val response = provider.generate("create a task")

        assertEquals("local", response.providerId)
        assertEquals(ModelRoute.Local, response.route)
        assertTrue(response.text.contains("create a task"))
    }

    @Test
    fun `provider receives standardized task context`() = runBlocking {
        val request = ModelRequest(
            prompt = "create a task",
            taskId = "task-123",
            metadata = mapOf("source" to "test")
        )
        val response = RecordingProvider().generate(request)

        assertEquals("task-123", response.taskId)
        assertEquals("test", response.metadata["source"])
    }

    @Test
    fun `registry exposes available local route`() {
        val registry = ModelProviderRegistry(listOf(provider))

        assertEquals(provider, registry.get(ModelRoute.Local))
        assertEquals(setOf(ModelRoute.Local), registry.availableRoutes())
    }

    @Test
    fun `registry reports only configured cloud providers as available`() {
        val registry = ModelProviderRegistry(
            listOf(
                provider,
                TestProvider("gemini", ModelRoute.Gemini, true),
                TestProvider("openrouter", ModelRoute.OpenRouter, false)
            )
        )

        assertEquals(
            ModelAvailability(geminiAvailable = true, openRouterAvailable = false),
            registry.availability()
        )
    }

    @Test
    fun `fallback routes skip unavailable providers and keep local last`() {
        val registry = ModelProviderRegistry(
            listOf(
                provider,
                TestProvider("gemini", ModelRoute.Gemini, false),
                TestProvider("openrouter", ModelRoute.OpenRouter, true)
            )
        )

        assertEquals(
            listOf(ModelRoute.OpenRouter, ModelRoute.Local),
            registry.fallbackRoutes(ModelRoute.OpenRouter)
        )
    }

    private class TestProvider(
        override val id: String,
        override val route: ModelRoute,
        override val isAvailable: Boolean
    ) : ModelProvider {
        override suspend fun generate(request: ModelRequest): ModelResponse =
            ModelResponse(request.prompt, id, route)
    }

    private data class RecordedResponse(
        val taskId: String?,
        val metadata: Map<String, String>
    )

    private class RecordingProvider : ModelProvider {
        override val id: String = "recording"
        override val route: ModelRoute = ModelRoute.Local
        override val isAvailable: Boolean = true
        var lastRequest: ModelRequest? = null

        override suspend fun generate(request: ModelRequest): ModelResponse {
            lastRequest = request
            return ModelResponse(
                text = request.prompt,
                providerId = id,
                route = route
            )
        }

        fun responseForLastRequest(): RecordedResponse {
            val request = requireNotNull(lastRequest)
            return RecordedResponse(request.taskId, request.metadata)
        }
    }
}
