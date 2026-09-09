package com.example.agent.nexus.agent

/**
 * Deterministic planner. Tool selection stays explicit so each execution path
 * is inspectable and can later be replaced by model-assisted planning.
 */
class AgentPlanner {
    fun plan(task: AgentTask, availableToolIds: Set<String> = emptySet()): AgentPlan {
        val input = task.input.lowercase()
        val toolId = when {
            ("開啟 app" in task.input || "打开 app" in task.input || "open app" in input || "launch app" in input) &&
                "app_agent" in availableToolIds -> "app_agent"
            "github" in input && "github" in availableToolIds -> "github"
            "网页" in task.input || "web" in input || "搜索" in task.input ->
                "web_research".takeIf(availableToolIds::contains)
            "文件" in task.input || "file" in input ->
                "file_agent".takeIf(availableToolIds::contains)
            ("memory" in input || "記憶" in task.input || "记忆" in task.input) && "memory" in availableToolIds ->
                "memory"
            ("skill" in input || "技能" in task.input) && "skill" in availableToolIds ->
                "skill"
            "local_task" in availableToolIds -> "local_task"
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
