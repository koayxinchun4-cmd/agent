package com.example.agent.nexus.agent

/**
 * Deterministic planner. Tool selection stays explicit so each execution path
 * is inspectable and can later be replaced by model-assisted planning.
 */
class AgentPlanner(
    private val intentClassifier: IntentClassifier = IntentClassifier(),
    private val taskDecomposer: TaskDecomposer = TaskDecomposer()
) {
    fun plan(task: AgentTask, availableToolIds: Set<String> = emptySet()): AgentPlan {
        val intent = intentClassifier.classify(task.input)
        val subtasks = taskDecomposer.decompose(task.input)
        val subtaskToolIds = subtasks.map { subtask ->
            toolForIntent(intentClassifier.classify(subtask), availableToolIds)
        }
        val toolId = subtaskToolIds.firstOrNull { it != null }
            ?: toolForIntent(intent, availableToolIds)

        val steps = buildList {
            add("understand_request")
            if (subtasks.size > 1) {
                subtasks.forEachIndexed { index, subtask ->
                    add("subtask:${index + 1}:$subtask")
                    subtaskToolIds.getOrNull(index)?.let { add("use_tool:$it") }
                }
            } else if (toolId != null) {
                add("use_tool:$toolId")
            }
            add("answer")
        }

        return AgentPlan(
            taskId = task.id,
            toolId = toolId,
            route = ModelRoute.Local,
            steps = steps,
            subtasks = subtasks,
            subtaskToolIds = subtaskToolIds
        )
    }

    private fun toolForIntent(
        intent: AgentIntent,
        availableToolIds: Set<String>
    ): String? = when (intent) {
        AgentIntent.App -> "app_agent".takeIf(availableToolIds::contains)
        AgentIntent.GitHub -> "github".takeIf(availableToolIds::contains)
        AgentIntent.Office -> "office".takeIf(availableToolIds::contains)
        AgentIntent.Memory -> "memory".takeIf(availableToolIds::contains)
        AgentIntent.Skills -> "skills".takeIf(availableToolIds::contains)
        AgentIntent.WebResearch -> "web_research".takeIf(availableToolIds::contains)
        AgentIntent.File -> "file_agent".takeIf(availableToolIds::contains)
        AgentIntent.General -> null
    } ?: "local_task".takeIf(availableToolIds::contains)
}
