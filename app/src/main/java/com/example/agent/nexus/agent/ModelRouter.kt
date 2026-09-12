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
        val input = task.input.lowercase()

        // Tool-backed tasks benefit from the stronger cloud route when available.
        if (plan.toolId != null && availability.openRouterAvailable) {
            return ModelRoute.OpenRouter
        }

        // Coding, research, diagnosis and architecture tasks are reasoning-heavy.
        if (REASONING_TASK_KEYWORDS.any(input::contains)) {
            if (availability.openRouterAvailable) return ModelRoute.OpenRouter
            if (availability.geminiAvailable) return ModelRoute.Gemini
        }

        // Summaries, translation and ordinary assistant requests prefer Gemini.
        if (GENERAL_ASSISTANT_KEYWORDS.any(input::contains)) {
            if (availability.geminiAvailable) return ModelRoute.Gemini
            if (availability.openRouterAvailable) return ModelRoute.OpenRouter
        }

        if (availability.geminiAvailable) return ModelRoute.Gemini
        if (availability.openRouterAvailable) return ModelRoute.OpenRouter
        return ModelRoute.Local
    }

    private companion object {
        val REASONING_TASK_KEYWORDS = setOf(
            "code", "coding", "debug", "github", "research", "analyze", "analysis",
            "architecture", "plan", "compare", "reason", "diagnose", "implement",
            "程式", "程式碼", "除錯", "研究", "分析", "架構", "比較", "診斷", "實作"
        )
        val GENERAL_ASSISTANT_KEYWORDS = setOf(
            "summarize", "summary", "translate", "translation", "rewrite", "explain",
            "摘要", "總結", "翻譯", "改寫", "解釋"
        )
    }
}

data class ModelAvailability(
    val geminiAvailable: Boolean = false,
    val openRouterAvailable: Boolean = false
)
