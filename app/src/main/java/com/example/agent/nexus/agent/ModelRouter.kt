package com.example.agent.nexus.agent

/**
 * Selects an AI backend without coupling Agent Core to a specific provider.
 * Provider availability is supplied by the app layer so secrets stay outside
 * the planner and router.
 */
class ModelRouter(
    private val availability: ModelAvailability = ModelAvailability()
) {
    fun route(task: AgentTask, plan: AgentPlan): ModelRoute {
        if (plan.toolId != null && availability.openRouterAvailable) {
            return ModelRoute.OpenRouter
        }

        val input = task.input.lowercase()
        val complexTask = COMPLEX_TASK_KEYWORDS.any(input::contains)
        if (complexTask) {
            if (availability.openRouterAvailable) return ModelRoute.OpenRouter
            if (availability.geminiAvailable) return ModelRoute.Gemini
        }

        if (availability.geminiAvailable) return ModelRoute.Gemini
        if (availability.openRouterAvailable) return ModelRoute.OpenRouter
        return ModelRoute.Local
    }

    private companion object {
        val COMPLEX_TASK_KEYWORDS = setOf(
            "code", "coding", "debug", "github", "research", "analyze", "analysis",
            "architecture", "plan", "compare", "reason", "diagnose", "implement"
        )
    }
}

data class ModelAvailability(
    val geminiAvailable: Boolean = false,
    val openRouterAvailable: Boolean = false
)
