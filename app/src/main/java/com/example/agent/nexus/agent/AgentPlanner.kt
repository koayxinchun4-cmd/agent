package com.example.agent.nexus.agent

/**
 * First deterministic planner. It deliberately avoids hidden model reasoning
 * and keeps tool selection small until the real model router is connected.
 */
class AgentPlanner {
    fun plan(task: AgentTask, availableToolIds: Set<String> = emptySet()): AgentPlan {
        val input = task.input.lowercase()
        val toolId = when {
            "github" in input && "github" in availableToolIds -> "github"
            "网页" in task.input || "web" in input || "搜索" in task.input ->
                "web_research".takeIf(availableToolIds::contains)
            "文件" in task.input || "file" in input ->
                "file_agent".takeIf(availableToolIds::contains)
            else -> null
        }

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
