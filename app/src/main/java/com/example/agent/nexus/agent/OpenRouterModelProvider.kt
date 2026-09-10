package com.example.agent.nexus.agent

import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.remote.OpenRouterMessage
import com.example.agent.data.remote.OpenRouterRequest

class OpenRouterModelProvider(
    private val apiService: OpenRouterApiService,
    private val apiKey: String,
    private val model: String = DEFAULT_OPENROUTER_MODEL,
    private val systemPrompt: String = NEXUS_SYSTEM_PROMPT
) : ModelProvider {
    override val id: String = "openrouter"
    override val route: ModelRoute = ModelRoute.OpenRouter
    override val isAvailable: Boolean = apiKey.isUsableSecret()

    override suspend fun generate(prompt: String): ModelResponse {
        require(isAvailable) { "OpenRouter API key is not configured" }
        require(model.isNotBlank()) { "OpenRouter model is not configured" }

        val response = apiService.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = OpenRouterRequest(
                model = model,
                messages = listOf(
                    OpenRouterMessage(role = "system", content = systemPrompt),
                    OpenRouterMessage(role = "user", content = prompt.trim())
                )
            )
        )
        val text = response.choices
            ?.asSequence()
            ?.mapNotNull { it.message?.content?.trim() }
            ?.firstOrNull { it.isNotEmpty() }
            ?: throw IllegalStateException("OpenRouter returned no text response")

        return ModelResponse(text = text, providerId = id, route = route)
    }

    companion object {
        const val DEFAULT_OPENROUTER_MODEL = "openrouter/auto"
    }
}
