package com.example.agent.nexus.agent

import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.remote.OpenRouterMessage
import com.example.agent.data.remote.OpenRouterRequest

/**
 * OpenRouter-backed provider. Credentials stay in the app layer and are
 * supplied explicitly by the caller rather than being owned by Agent Core.
 */
class OpenRouterModelProvider(
    private val apiService: OpenRouterApiService,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL
) : ModelProvider {
    override val id: String = "openrouter"
    override val route: ModelRoute = ModelRoute.OpenRouter
    override val isAvailable: Boolean = isConfigured(apiKey, model)

    override suspend fun generate(prompt: String): ModelResponse {
        require(isAvailable) { "OpenRouter provider is not configured" }

        val request = OpenRouterRequest(
            model = model.trim(),
            messages = listOf(
                OpenRouterMessage(role = "user", content = prompt.trim())
            )
        )
        val response = apiService.createChatCompletion(
            authorization = "Bearer ${apiKey.trim()}",
            request = request
        )
        val text = response.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("OpenRouter returned no text response")

        return ModelResponse(
            text = text,
            providerId = id,
            route = route
        )
    }

    companion object {
        const val DEFAULT_MODEL = "openrouter/auto"
        private const val PLACEHOLDER_KEY = "MY_OPENROUTER_API_KEY"

        fun isConfigured(apiKey: String, model: String = DEFAULT_MODEL): Boolean =
            apiKey.trim().isNotEmpty() &&
                apiKey.trim() != PLACEHOLDER_KEY &&
                model.trim().isNotEmpty()
    }
}
