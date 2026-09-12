package com.example.agent.nexus.agent

import com.example.agent.nexus.memory.AgentExecutionContext
import com.example.agent.nexus.memory.DefaultMemoryContextProvider
import com.example.agent.nexus.memory.MemoryContextProvider
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
    private val loopConfig: AgentLoopConfig = AgentLoopConfig(),
    private val recoveryPolicy: AgentRecoveryPolicy = AgentRecoveryPolicy(),
    private val modelProviders: ModelProviderRegistry = ModelProviderRegistry(
        listOf(LocalModelProvider())
    ),
    private val memoryContextProvider: MemoryContextProvider = DefaultMemoryContextProvider()
) {
    suspend fun execute(task: AgentTask): AgentResult =
        executeDetailed(task).result

    suspend fun executeDetailed(
        task: AgentTask,
        onProgress: (AgentProgress) -> Unit = {}
    ): AgentExecution {
        var currentTask = task
        val projectId = task.metadata[AgentTask.PROJECT_ID]
        val executionContext = AgentExecutionContext(
            taskId = task.id,
            projectId = projectId,
            memory = memoryContextProvider.load(
                taskId = task.id,
                projectId = projectId
            ),
            metadata = task.metadata
        )
        var plan = planAndRoute(currentTask)
        val steps = mutableListOf<AgentStepResult>()

        fun emit(step: AgentStepResult, attempt: Int = 0) {
            steps += step
            onProgress(AgentProgress(step = step, attempt = attempt, steps = steps.toList()))
        }

        emit(AgentStepResult("understand_request", true, "Request understood"))
        emit(AgentStepResult("memory_context", true, "Loaded ${executionContext.memory.items.size} memory item(s)"))
        emit(AgentStepResult("plan", true, "Selected ${plan.toolId ?: "direct answer"} execution path"))

        val toolId = plan.toolId
        if (toolId == null) {
            val fallbackRoutes = modelProviders.fallbackRoutes(plan.route)
            if (fallbackRoutes.isEmpty()) {
                val result = AgentResult.Failure("No model provider is available")
                emit(AgentStepResult("answer", false, result.message))
                return AgentExecution(plan, steps, result, context = executionContext)
            }

            var lastError: Throwable? = null
            for ((index, route) in fallbackRoutes.withIndex()) {
                val provider = modelProviders.get(route) ?: continue
                if (route != plan.route) {
                    plan = plan.copy(route = route)
                    emit(
                        AgentStepResult(
                            "fallback:${provider.id}",
                            true,
                            "Primary model unavailable; falling back to ${provider.id}"
                        )
                    )
                }
                try {
                    emit(AgentStepResult("model:${provider.id}", true, "Generating response"))
                    val response = provider.generate(
                        ModelRequest(
                            prompt = currentTask.input,
                            taskId = currentTask.id,
                            metadata = currentTask.metadata
                        )
                    )
                    val result = AgentResult.Success(response.text)
                    emit(AgentStepResult("verify", true, "Response generated successfully"))
                    emit(AgentStepResult("answer", true, result.text))
                    return AgentExecution(plan, steps, result, context = executionContext)
                } catch (error: Throwable) {
                    lastError = error
                    if (index < fallbackRoutes.lastIndex) {
                        emit(
                            AgentStepResult(
                                "model:${provider.id}",
                                false,
                                "Provider failed; trying next available provider"
                            )
                        )
                    }
                }
            }

            val result = AgentResult.Failure(
                "All available model providers failed: ${lastError?.message ?: "unknown error"}",
                lastError
            )
            emit(AgentStepResult("answer", false, result.message))
            return AgentExecution(plan, steps, result, context = executionContext)
        }

        var lastResult: ToolResult = ToolResult.Failure("Tool has not executed")
        var attempts = 0

        while (attempts < loopConfig.maxAttempts) {
            attempts += 1
            val attemptStep = "use_tool:$toolId#attempt$attempts"
            lastResult = toolRegistry.execute(toolId, currentTask)
            emit(lastResult.toAgentStep(attemptStep), attempts)

            when (val verification = verifier.verify(lastResult)) {
                VerificationResult.Passed -> {
                    val result = AgentResult.Success(
                        "${(lastResult as ToolResult.Success).text}\n\n[Model route: ${plan.route}]"
                    )
                    emit(AgentStepResult("verify", true, "Verification passed"), attempts)
                    emit(AgentStepResult("answer", true, result.text), attempts)
                    return AgentExecution(plan, steps, result, attempts, executionContext)
                }

                is VerificationResult.Retry -> {
                    val diagnosis = when (lastResult) {
                        is ToolResult.Failure -> recoveryPolicy.diagnose(lastResult)
                        is ToolResult.Success -> recoveryPolicy.diagnoseVerification(verification.reason)
                    }
                    emit(
                        AgentStepResult(
                            "diagnose",
                            false,
                            when (diagnosis) {
                                is FailureDiagnosis.Transient -> "Transient failure: ${diagnosis.reason}"
                                is FailureDiagnosis.Permanent -> "Permanent failure: ${diagnosis.reason}"
                            }
                        ),
                        attempts
                    )

                    val canRetry = attempts < loopConfig.maxAttempts &&
                        diagnosis is FailureDiagnosis.Transient
                    if (!canRetry) {
                        emit(
                            AgentStepResult(
                                "verify",
                                false,
                                if (diagnosis is FailureDiagnosis.Permanent) {
                                    "Recovery stopped: failure is permanent"
                                } else {
                                    "Recovery stopped: maximum attempts reached"
                                }
                            ),
                            attempts
                        )
                        break
                    }

                    currentTask = recoveryPolicy.adjust(currentTask, diagnosis, attempts)
                    plan = planAndRoute(currentTask)
                    emit(
                        AgentStepResult(
                            "adjust_plan",
                            true,
                            "Adjusted input for recovery attempt ${attempts + 1}"
                        ),
                        attempts
                    )
                    emit(
                        AgentStepResult(
                            "verify",
                            false,
                            "Recovery prepared; retrying"
                        ),
                        attempts
                    )
                }
            }
        }

        val failureMessage = when (lastResult) {
            is ToolResult.Failure -> lastResult.message
            is ToolResult.Success -> "Tool result did not pass verification"
        }
        val result = AgentResult.Failure(
            "$failureMessage (model route: ${plan.route}; attempts: $attempts)",
            (lastResult as? ToolResult.Failure)?.cause
        )
        emit(AgentStepResult("answer", false, result.message), attempts)
        return AgentExecution(plan, steps, result, attempts, executionContext)
    }

    private fun planAndRoute(task: AgentTask): AgentPlan {
        val initialPlan = planner.plan(
            task = task,
            availableToolIds = toolRegistry.list().map { it.id }.toSet()
        )
        val routedPlan = initialPlan.copy(route = modelRouter.route(task, initialPlan))
        val provider = modelProviders.get(routedPlan.route)
            ?.takeIf { it.isAvailable }
            ?: modelProviders.get(ModelRoute.Local)
        return if (provider != null && provider.route != routedPlan.route) {
            routedPlan.copy(route = provider.route)
        } else {
            routedPlan
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
