package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult

/**
 * Core orchestration layer: plan first, select a model route, then execute a
 * registered tool when the plan requires one.
 */
class NexusAgent(
    private val toolRegistry: ToolRegistry,
    private val planner: AgentPlanner = AgentPlanner(),
    private val modelRouter: ModelRouter = ModelRouter()
) {
    suspend fun execute(task: AgentTask): AgentResult {
        val initialPlan = planner.plan(
            task = task,
            availableToolIds = toolRegistry.list().map { it.id }.toSet()
        )
        val route = modelRouter.route(task, initialPlan)
        val plan = initialPlan.copy(route = route)

        val toolId = plan.toolId ?: return AgentResult.Success(
            "任务已规划：${task.input}（模型路线：${plan.route}）"
        )

        return when (val result = toolRegistry.execute(toolId, task)) {
            is ToolResult.Success -> AgentResult.Success(
                "${result.text}\n\n[模型路线：${plan.route}]"
            )
            is ToolResult.Failure -> AgentResult.Failure(
                "${result.message}（模型路线：${plan.route}）",
                result.cause
            )
        }
    }

    /**
     * Backward-compatible entry point for callers that explicitly select a
     * tool. The normal Agent path should use [execute] so planning stays central.
     */
    suspend fun execute(task: AgentTask, toolId: String): AgentResult {
        return when (val result = toolRegistry.execute(toolId, task)) {
            is ToolResult.Success -> AgentResult.Success(result.text)
            is ToolResult.Failure -> AgentResult.Failure(result.message, result.cause)
        }
    }
}
