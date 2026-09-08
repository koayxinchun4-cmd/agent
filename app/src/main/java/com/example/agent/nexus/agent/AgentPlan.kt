package com.example.agent.nexus.agent

/** A compact, executable plan produced for an Agent task. */
data class AgentPlan(
    val taskId: String,
    val toolId: String? = null,
    val route: ModelRoute = ModelRoute.Local,
    val steps: List<String> = listOf("answer")
)

enum class ModelRoute {
    Gemini,
    OpenRouter,
    Local
}
