package com.example.agent.nexus.agent

import com.example.agent.data.remote.Candidate
import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.GeminiResponse
import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.remote.OpenRouterChoice
import com.example.agent.data.remote.OpenRouterMessage
import com.example.agent.data.remote.OpenRouterRequest
import com.example.agent.data.remote.OpenRouterResponse
import com.example.agent.data.remote.Part
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelProviderTest {
    private val localProvider = LocalModelProvider()

    @Test
    fun `local provider is always available`() {
        assertTrue(localProvider.isAvailable)
        assertEquals(ModelRoute.Local, localProvider.route)
    }

    @Test
    fun `local provider returns actionable response`() = runBlocking {
        val response = localProvider.generate("create a task")

        assertEquals("local", response.providerId)
        assertEquals(ModelRoute.Local, response.route)
        assertTrue(response.text.contains("create a task"))
    }

    @Test
    fun `registry exposes available local route`() {
        val registry = ModelProviderRegistry(listOf(localProvider))

        assertEquals(localProvider, registry.get(ModelRoute.Local))
        assertEquals(setOf(ModelRoute.Local), registry.availableRoutes())
    }

    @Test
    fun `cloud providers reject placeholder secrets`() {
        val gemini = GeminiModelProvider(FakeGeminiApiService(), "MY_GEMINI_API_KEY")
        val openRouter = OpenRouterModelProvider(FakeOpenRouterApiService(), "MY_OPENROUTER_API_KEY")

        assertFalse(gemini.isAvailable)
        assertFalse(openRouter.isAvailable)
    }

    @Test
    fun `gemini provider maps remote response into model response`() = runBlocking {
        val provider = GeminiModelProvider(
            apiService = FakeGeminiApiService(
                GeminiResponse(
                    candidates = listOf(
                        Candidate(
                            Content(
                                role = "model",
                                parts = listOf(Part("Gemini says hello"))
                            )
                        )
                    )
                )
            ),
            apiKey = "test-gemini-key"
        )

        val response = provider.generate("hello")

        assertEquals("gemini", response.providerId)
        assertEquals(ModelRoute.Gemini, response.route)
        assertEquals("Gemini says hello", response.text)
    }

    @Test
    fun `openrouter provider maps remote response into model response`() = runBlocking {
        val provider = OpenRouterModelProvider(
            apiService = FakeOpenRouterApiService(
                OpenRouterResponse(
                    choices = listOf(
                        OpenRouterChoice(
                            OpenRouterMessage(role = "assistant", content = "OpenRouter says hello")
                        )
                    )
                )
            ),
            apiKey = "test-openrouter-key"
        )

        val response = provider.generate("hello")

        assertEquals("openrouter", response.providerId)
        assertEquals(ModelRoute.OpenRouter, response.route)
        assertEquals("OpenRouter says hello", response.text)
    }
}

private class FakeGeminiApiService(
    private val response: GeminiResponse = GeminiResponse(candidates = emptyList())
) : GeminiApiService {
    override suspend fun generateContent(apiKey: String, request: GeminiRequest): GeminiResponse = response
}

private class FakeOpenRouterApiService(
    private val response: OpenRouterResponse = OpenRouterResponse(choices = emptyList())
) : OpenRouterApiService {
    override suspend fun createChatCompletion(
        authorization: String,
        request: OpenRouterRequest
    ): OpenRouterResponse = response
}
