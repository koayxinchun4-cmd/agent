package com.example.agent.nexus.agent

/**
 * Provider abstraction for model-backed Agent responses.
 * A provider owns one model backend; Agent Core only depends on this contract.
 */
interface ModelProvider {
    val id: String
    val route: ModelRoute
    val isAvailable: Boolean

    suspend fun generate(prompt: String): ModelResponse
}

data class ModelResponse(
    val text: String,
    val providerId: String,
    val route: ModelRoute
)

class ModelProviderRegistry(
    providers: List<ModelProvider> = emptyList()
) {
    private val providersByRoute = providers.associateBy { it.route }

    fun get(route: ModelRoute): ModelProvider? = providersByRoute[route]

    fun availableRoutes(): Set<ModelRoute> =
        providersByRoute.values.filter { it.isAvailable }.map { it.route }.toSet()

    fun availability(): ModelAvailability = ModelAvailability(
        geminiAvailable = get(ModelRoute.Gemini)?.isAvailable == true,
        openRouterAvailable = get(ModelRoute.OpenRouter)?.isAvailable == true
    )

    /**
     * Returns an ordered list of usable providers for a requested route.
     * The requested route is preferred, followed by other cloud providers,
     * with Local always acting as the final safe fallback when registered.
     */
    fun fallbackRoutes(preferred: ModelRoute): List<ModelRoute> {
        val order = listOf(
            preferred,
            ModelRoute.Gemini,
            ModelRoute.OpenRouter,
            ModelRoute.Local
        )
        return order.distinct().filter { route ->
            providersByRoute[route]?.isAvailable == true
        }
    }
}

/**
 * Safe deterministic fallback used when no cloud model is configured.
 * It makes the Agent path executable without pretending to be a remote LLM.
 */
class LocalModelProvider : ModelProvider {
    override val id: String = "local"
    override val route: ModelRoute = ModelRoute.Local
    override val isAvailable: Boolean = true

    override suspend fun generate(prompt: String): ModelResponse {
        val normalized = prompt.trim()
        val text = if (normalized.isEmpty()) {
            "Please provide a task for Nexus to execute."
        } else {
            "Nexus received this task and is ready to execute it:\n\n$normalized"
        }
        return ModelResponse(text = text, providerId = id, route = ModelRoute.Local)
    }
}
