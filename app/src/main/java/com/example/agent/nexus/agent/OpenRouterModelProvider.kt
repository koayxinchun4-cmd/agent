package com.example.agent.nexus.agent

import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.remote.OpenRouterMessage
import com.example.agent.data.remote.OpenRouterRequest
import kotlinx.coroutines.withTimeout

/**
 * OpenRouter-backed provider. Credentials stay in the app layer and are
 * supplied explicitly by the caller rather than being owned by Agent Core.
 */
class OpenRouterModelProvider(
    private val apiService: OpenRouterApiService,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS
) : ModelProvider {
    override val id: String = "openrouter"
    override val route: ModelRoute = ModelRoute.OpenRouter
    override val isAvailable: Boolean = isConfigured(apiKey, model)

    override suspend fun generate(request: ModelRequest): ModelResponse {
        require(isAvailable) { "OpenRouter provider is not configured" }
        require(request.prompt.isNotBlank()) { "Prompt cannot be blank" }

        val apiRequest = OpenRouterRequest(
            model = model.trim(),
            messages = listOf(
                OpenRouterMessage(role = "system", content = NexusSystemPrompt.text),
                OpenRouterMessage(role = "user", content = request.prompt.trim())
            )
        )

        return withTimeout(timeoutMs) {
            runCatching {
                apiService.createChatCompletion(
                    authorization = "Bearer ${apiKey.trim()}",
                    request = apiRequest
                )
            }.getOrElse { error -> throw ProviderRequestException(id, error) }
                .let { response ->
                    val text = response.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?.trim()
                        .orEmpty()
                    if (text.isBlank()) {
                        throw ProviderRequestException(id, "OpenRouter returned an empty response")
                    }
                    ModelResponse(text = text, providerId = id, route = route)
                }
        }
    }

    companion object {
        const val DEFAULT_MODEL = "openrouter/auto"
        private const val DEFAULT_TIMEOUT_MS = 20_000L
        private const val PLACEHOLDER_KEY = "MY_OPENROUTER_API_KEY"

        fun isConfigured(apiKey: String, model: String = DEFAULT_MODEL): Boolean =
            apiKey.trim().isNotEmpty() &&
                apiKey.trim() != PLACEHOLDER_KEY &&
                model.trim().isNotEmpty()
    }
}
