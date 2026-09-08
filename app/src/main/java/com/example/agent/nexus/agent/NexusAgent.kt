package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult

/**
 * Core orchestration layer: plan first, select a model route, then execute and
 * verify a registered tool when the plan requires one.
 */
class NexusAgent(
    private val toolRegistry: ToolRegistry,
    private val planner: AgentPlanner = AgentPlanner(),
    private val modelRouter: ModelRouter = ModelRouter(),
    private val verifier: AgentVerifier = AgentVerifier(),
    private val loopConfig: AgentLoopConfig = AgentLoopConfig()
) {
    suspend fun execute(task: AgentTask): AgentResult =
        executeDetailed(task).result

    suspend fun executeDetailed(
        task: AgentTask,
        onProgress: (AgentProgress) -> Unit = {}
    ): AgentExecution {
        val initialPlan = planner.plan(
            task = task,
            availableToolIds = toolRegistry.list().map { it.id }.toSet()
        )
        val route = modelRouter.route(task, initialPlan)
        val plan = initialPlan.copy(route = route)
        val steps = mutableListOf<AgentStepResult>()

        fun emit(step: AgentStepResult, attempt: Int = 0) {
            steps += step
            onProgress(AgentProgress(step = step, attempt = attempt, steps = steps.toList()))
        }

        emit(AgentStepResult("understand_request", true, "任务已理解"))
        emit(AgentStepResult("plan", true, "已选择 ${plan.toolId ?: "直接回答"} 执行路径"))

        val toolId = plan.toolId
        if (toolId == null) {
            val result = AgentResult.Success(
                "任务已规划：${task.input}（模型路线：${plan.route}）"
            )
            emit(AgentStepResult("answer", true, result.text))
            return AgentExecution(plan, steps, result)
        }

        var lastResult: ToolResult = ToolResult.Failure("工具尚未执行")
        var attempts = 0

        while (attempts < loopConfig.maxAttempts) {
            attempts += 1
            val attemptStep = "use_tool:$toolId#attempt$attempts"
            lastResult = toolRegistry.execute(toolId, task)
            emit(lastResult.toAgentStep(attemptStep), attempts)

            when (val verification = verifier.verify(lastResult)) {
                VerificationResult.Passed -> {
                    val result = AgentResult.Success(
                        "${(lastResult as ToolResult.Success).text}\n\n[模型路线：${plan.route}]"
                    )
                    emit(AgentStepResult("verify", true, "验证通过"), attempts)
                    emit(AgentStepResult("answer", true, result.text), attempts)
                    return AgentExecution(plan, steps, result, attempts)
                }

                is VerificationResult.Retry -> {
                    val canRetry = attempts < loopConfig.maxAttempts
                    emit(
                        AgentStepResult(
                            "verify",
                            false,
                            if (canRetry) {
                                "验证未通过：${verification.reason}；准备重试"
                            } else {
                                "验证未通过：${verification.reason}；已达到最大尝试次数"
                            }
                        ),
                        attempts
                    )
                }
            }
        }

        val failureMessage = when (lastResult) {
            is ToolResult.Failure -> lastResult.message
            is ToolResult.Success -> "工具结果未通过验证"
        }
        val result = AgentResult.Failure(
            "$failureMessage（模型路线：${plan.route}；尝试次数：$attempts）",
            (lastResult as? ToolResult.Failure)?.cause
        )
        emit(AgentStepResult("answer", false, result.message), attempts)
        return AgentExecution(plan, steps, result, attempts)
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
