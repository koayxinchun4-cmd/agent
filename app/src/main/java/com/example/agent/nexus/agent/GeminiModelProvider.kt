package com.example.agent.nexus.agent

import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Part
import kotlinx.coroutines.withTimeout

/**
 * Gemini-backed provider. API credentials stay in the app layer and are
 * supplied explicitly by the caller rather than being owned by Agent Core.
 */
class GeminiModelProvider(
    private val apiService: GeminiApiService,
    private val apiKey: String,
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS
) : ModelProvider {
    override val id: String = "gemini"
    override val route: ModelRoute = ModelRoute.Gemini
    override val isAvailable: Boolean = isConfigured(apiKey)

    override suspend fun generate(request: ModelRequest): ModelResponse {
        require(isAvailable) { "Gemini provider is not configured" }
        require(request.prompt.isNotBlank()) { "Prompt cannot be blank" }

        val apiRequest = GeminiRequest(
            systemInstruction = Content(parts = listOf(Part(text = NexusSystemPrompt.text))),
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = request.prompt.trim()))
                )
            )
        )

        return withTimeout(timeoutMs) {
            runCatching { apiService.generateContent(apiKey.trim(), apiRequest) }
                .getOrElse { error -> throw ProviderRequestException(id, error) }
                .let { response ->
                    val text = response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        ?.trim()
                        .orEmpty()
                    if (text.isBlank()) {
                        throw ProviderRequestException(id, "Gemini returned an empty response")
                    }
                    ModelResponse(text = text, providerId = id, route = route)
                }
        }
    }

    companion object {
        private const val DEFAULT_TIMEOUT_MS = 20_000L
        fun isConfigured(apiKey: String): Boolean =
            apiKey.trim().isNotEmpty() && apiKey.trim() != "MY_GEMINI_API_KEY"
    }
}
