package com.example.agent.nexus.agent

import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Part

class GeminiModelProvider(
    private val apiService: GeminiApiService,
    private val apiKey: String,
    private val systemPrompt: String = NEXUS_SYSTEM_PROMPT
) : ModelProvider {
    override val id: String = "gemini"
    override val route: ModelRoute = ModelRoute.Gemini
    override val isAvailable: Boolean = apiKey.isUsableSecret()

    override suspend fun generate(prompt: String): ModelResponse {
        require(isAvailable) { "Gemini API key is not configured" }
        val response = apiService.generateContent(
            apiKey = apiKey,
            request = GeminiRequest(
                systemInstruction = Content(parts = listOf(Part(systemPrompt))),
                contents = listOf(
                    Content(role = "user", parts = listOf(Part(prompt.trim())))
                )
            )
        )
        val text = response.candidates
            ?.asSequence()
            ?.flatMap { it.content?.parts.orEmpty().asSequence() }
            ?.map { it.text.trim() }
            ?.firstOrNull { it.isNotEmpty() }
            ?: throw IllegalStateException("Gemini returned no text response")

        return ModelResponse(text = text, providerId = id, route = route)
    }
}

internal fun String.isUsableSecret(): Boolean =
    isNotBlank() && !equals("MY_GEMINI_API_KEY", ignoreCase = true) &&
        !equals("MY_OPENROUTER_API_KEY", ignoreCase = true)
