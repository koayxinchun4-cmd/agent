package com.example.agent.nexus.agent

import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Part

/**
 * Gemini-backed provider. API credentials stay in the app layer and are
 * supplied explicitly by the caller rather than being owned by Agent Core.
 */
class GeminiModelProvider(
    private val apiService: GeminiApiService,
    private val apiKey: String
) : ModelProvider {
    override val id: String = "gemini"
    override val route: ModelRoute = ModelRoute.Gemini
    override val isAvailable: Boolean = isConfigured(apiKey)

    override suspend fun generate(request: ModelRequest): ModelResponse {
        require(isAvailable) { "Gemini provider is not configured" }

        val apiRequest = GeminiRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = request.prompt.trim()))
                )
            )
        )
        val response = apiService.generateContent(apiKey.trim(), apiRequest)
        val text = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("Gemini returned no text response")

        return ModelResponse(
            text = text,
            providerId = id,
            route = route
        )
    }

    companion object {
        fun isConfigured(apiKey: String): Boolean =
            apiKey.trim().isNotEmpty() && apiKey.trim() != "MY_GEMINI_API_KEY"
    }
}
