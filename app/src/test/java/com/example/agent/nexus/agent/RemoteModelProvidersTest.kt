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

class RemoteModelProvidersTest {
    @Test
    fun `placeholder keys make remote providers unavailable`() {
        val gemini = GeminiModelProvider(FakeGeminiApiService(), "MY_GEMINI_API_KEY")
        val openRouter = OpenRouterModelProvider(FakeOpenRouterApiService(), "MY_OPENROUTER_API_KEY")

        assertFalse(gemini.isAvailable)
        assertFalse(openRouter.isAvailable)
    }

    @Test
    fun `gemini provider returns generated text and shared system prompt`() = runBlocking {
        val api = FakeGeminiApiService("hello from gemini")
        val provider = GeminiModelProvider(api, "real-gemini-key-1234567890")

        val response = provider.generate(ModelRequest("hello"))

        assertTrue(provider.isAvailable)
        assertEquals("hello from gemini", response.text)
        assertEquals(ModelRoute.Gemini, response.route)
        assertEquals(NexusSystemPrompt.text, api.lastRequest?.systemInstruction?.parts?.first()?.text)
    }

    @Test
    fun `openrouter provider returns generated text and shared system prompt`() = runBlocking {
        val api = FakeOpenRouterApiService("hello from router")
        val provider = OpenRouterModelProvider(
            api,
            "real-openrouter-key-1234567890"
        )

        val response = provider.generate(ModelRequest("hello"))

        assertTrue(provider.isAvailable)
        assertEquals("hello from router", response.text)
        assertEquals(ModelRoute.OpenRouter, response.route)
        assertEquals(NexusSystemPrompt.text, api.lastRequest?.messages?.first()?.content)
    }

    @Test
    fun `provider rejects blank prompts`() = runBlocking {
        val provider = GeminiModelProvider(FakeGeminiApiService(), "real-gemini-key-1234567890")

        val error = runCatching { provider.generate(ModelRequest("   ")) }.exceptionOrNull()

        assertEquals("Prompt cannot be blank", error?.message)
    }

    private class FakeGeminiApiService(
        private val text: String = "generated"
    ) : GeminiApiService {
        var lastRequest: GeminiRequest? = null

        override suspend fun generateContent(
            apiKey: String,
            request: GeminiRequest
        ): GeminiResponse {
            lastRequest = request
            return GeminiResponse(
                candidates = listOf(Candidate(Content(role = "model", parts = listOf(Part(text)))))
            )
        }
    }

    private class FakeOpenRouterApiService(
        private val text: String = "generated"
    ) : OpenRouterApiService {
        var lastRequest: OpenRouterRequest? = null

        override suspend fun createChatCompletion(
            authorization: String,
            request: OpenRouterRequest
        ): OpenRouterResponse {
            lastRequest = request
            return OpenRouterResponse(
                choices = listOf(OpenRouterChoice(OpenRouterMessage("assistant", text)))
            )
        }
    }
}
