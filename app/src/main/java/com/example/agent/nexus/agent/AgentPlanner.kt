package com.example.agent.nexus.agent

/**
 * Deterministic planner. Tool selection stays explicit so each execution path
 * is inspectable and can later be replaced by model-assisted planning.
 */
class AgentPlanner(
    private val intentClassifier: IntentClassifier = IntentClassifier()
) {
    fun plan(task: AgentTask, availableToolIds: Set<String> = emptySet()): AgentPlan {
        val intent = intentClassifier.classify(task.input)
        val toolId = when (intent) {
            AgentIntent.App -> "app_agent".takeIf(availableToolIds::contains)
            AgentIntent.GitHub -> "github".takeIf(availableToolIds::contains)
            AgentIntent.Office -> "office".takeIf(availableToolIds::contains)
            AgentIntent.Memory -> "memory".takeIf(availableToolIds::contains)
            AgentIntent.Skills -> "skills".takeIf(availableToolIds::contains)
            AgentIntent.WebResearch -> "web_research".takeIf(availableToolIds::contains)
            AgentIntent.File -> "file_agent".takeIf(availableToolIds::contains)
            AgentIntent.General -> null
        } ?: "local_task".takeIf(availableToolIds::contains)

        val steps = if (toolId == null) {
            listOf("understand_request", "answer")
        } else {
            listOf("understand_request", "use_tool:$toolId", "answer")
        }

        return AgentPlan(
            taskId = task.id,
            toolId = toolId,
            route = ModelRoute.Local,
            steps = steps
        )
    }
}
