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
    suspend fun execute(task: AgentTask): AgentResult =
        executeDetailed(task).result

    /**
     * Runs the task and keeps an explicit execution trace for future UI,
     * verification, retry and Agent Moments integrations.
     */
    suspend fun executeDetailed(task: AgentTask): AgentExecution {
        val initialPlan = planner.plan(
            task = task,
            availableToolIds = toolRegistry.list().map { it.id }.toSet()
        )
        val route = modelRouter.route(task, initialPlan)
        val plan = initialPlan.copy(route = route)
        val steps = mutableListOf<AgentStepResult>()

        steps += AgentStepResult("understand_request", true, "任务已理解")

        val toolId = plan.toolId
        if (toolId == null) {
            val result = AgentResult.Success(
                "任务已规划：${task.input}（模型路线：${plan.route}）"
            )
            steps += AgentStepResult("answer", true, result.text)
            return AgentExecution(plan, steps, result)
        }

        val toolResult = toolRegistry.execute(toolId, task)
        steps += toolResult.toAgentStep("use_tool:$toolId")

        val result = when (toolResult) {
            is ToolResult.Success -> AgentResult.Success(
                "${toolResult.text}\n\n[模型路线：${plan.route}]"
            )
            is ToolResult.Failure -> AgentResult.Failure(
                "${toolResult.message}（模型路线：${plan.route}）",
                toolResult.cause
            )
        }
        steps += AgentStepResult("answer", result is AgentResult.Success, result.toText())
        return AgentExecution(plan, steps, result)
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

private fun AgentResult.toText(): String = when (this) {
    is AgentResult.Success -> text
    is AgentResult.Failure -> message
}
