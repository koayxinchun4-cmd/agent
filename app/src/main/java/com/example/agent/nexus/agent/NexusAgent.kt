package com.example.agent.nexus.agent

import com.example.agent.nexus.memory.AgentExecutionContext
import com.example.agent.nexus.memory.DefaultMemoryContextProvider
import com.example.agent.nexus.memory.MemoryContextProvider
import com.example.agent.nexus.tool.RiskLevel
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult

/**
 * Core orchestration layer: plan first, select a model route, then execute and
 * verify registered tools. Multi-step plans execute their tool-backed subtasks
 * in order so the planner contract matches actual execution.
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

    /**
     * Preflight all planned tools before execution. The UI can use the first
     * request to obtain explicit user confirmation before invoking the plan.
     */
    fun previewConfirmation(task: AgentTask): AgentConfirmationRequest? {
        if (task.metadata[AgentTask.CONFIRMATION_GRANTED] == "true") return null
        val plan = planAndRoute(task)
        return plannedToolIds(plan).asSequence()
            .mapNotNull(toolRegistry::get)
            .firstOrNull { it.riskLevel == RiskLevel.REQUIRES_CONFIRMATION }
            ?.let { tool -> AgentConfirmationRequest(task = task, tool = tool, plan = plan) }
    }

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

        val plannedToolIds = plannedToolIds(plan)
        val hasToolBackedSubtask = plannedToolIds.isNotEmpty()
        if (!hasToolBackedSubtask) {
            return generateModelAnswer(
                task = currentTask,
                plan = plan,
                steps = steps,
                emit = ::emit,
                executionContext = executionContext
            )
        }

        val observations = mutableListOf<String>()
        var totalAttempts = 0
        var lastToolResult: ToolResult? = null

        for (index in plan.subtasks.indices) {
            val subtask = plan.subtasks[index]
            val toolId = plan.subtaskToolIds.getOrNull(index)
                ?: plan.toolId.takeIf { plan.subtasks.size == 1 }
            if (toolId == null) continue

            val subtaskTask = currentTask.copy(input = subtask)
            var attempts = 0
            var result: ToolResult = ToolResult.Failure("Tool has not executed")

            while (attempts < loopConfig.maxAttempts) {
                attempts += 1
                totalAttempts += 1
                val attemptStep = "use_tool:$toolId#attempt$attempts"
                result = toolRegistry.execute(toolId, subtaskTask)
                emit(result.toAgentStep(attemptStep), attempts)

                when (val verification = verifier.verify(result)) {
                    VerificationResult.Passed -> {
                        emit(AgentStepResult("verify", true, "Verification passed for subtask ${index + 1}"), attempts)
                        observations += "Subtask ${index + 1} ($subtask) result:\n${(result as ToolResult.Success).text}"
                        break
                    }

                    is VerificationResult.Retry -> {
                        val diagnosis = when (result) {
                            is ToolResult.Failure -> recoveryPolicy.diagnose(result)
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

                        val canRetry = attempts < loopConfig.maxAttempts && diagnosis is FailureDiagnosis.Transient
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
                        emit(AgentStepResult("adjust_plan", true, "Adjusted input for recovery attempt ${attempts + 1}"), attempts)
                        emit(AgentStepResult("verify", false, "Recovery prepared; retrying"), attempts)
                    }
                }
            }

            lastToolResult = result
            if (result !is ToolResult.Success || result.text.isBlank()) {
                val failureMessage = when (result) {
                    is ToolResult.Failure -> result.message
                    is ToolResult.Success -> "Tool result did not pass verification"
                }
                val failure = AgentResult.Failure(
                    "$failureMessage (model route: ${plan.route}; attempts: $totalAttempts)",
                    (result as? ToolResult.Failure)?.cause
                )
                emit(AgentStepResult("answer", false, failure.message), attempts)
                return AgentExecution(plan, steps, failure, totalAttempts, executionContext)
            }
        }

        // If the final planned subtask is answer-only, use the selected model to
        // synthesize all verified observations instead of returning raw tool data.
        val finalSubtask = plan.subtasks.lastOrNull()
        val finalToolId = if (plan.subtasks.size == 1) {
            plan.subtaskToolIds.firstOrNull() ?: plan.toolId
        } else {
            plan.subtaskToolIds.lastOrNull()
        }
        if (finalSubtask != null && finalToolId == null && observations.isNotEmpty()) {
            val synthesisTask = currentTask.copy(
                input = buildString {
                    appendLine(finalSubtask)
                    appendLine()
                    appendLine("Verified observations:")
                    append(observations.joinToString("\n\n"))
                }
            )
            return generateModelAnswer(
                task = synthesisTask,
                plan = plan,
                steps = steps,
                emit = ::emit,
                executionContext = executionContext,
                attempts = totalAttempts
            )
        }

        val combined = observations.joinToString("\n\n")
        val result = if (combined.isNotBlank()) {
            AgentResult.Success("$combined\n\n[Model route: ${plan.route}]")
        } else {
            AgentResult.Failure(
                "No verified tool result was produced (model route: ${plan.route}; attempts: $totalAttempts)",
                (lastToolResult as? ToolResult.Failure)?.cause
            )
        }
        emit(AgentStepResult("answer", result is AgentResult.Success, result.message), totalAttempts)
        return AgentExecution(plan, steps, result, totalAttempts, executionContext)
    }

    private suspend fun generateModelAnswer(
        task: AgentTask,
        plan: AgentPlan,
        steps: MutableList<AgentStepResult>,
        emit: (AgentStepResult, Int) -> Unit,
        executionContext: AgentExecutionContext,
        attempts: Int = 0
    ): AgentExecution {
        val fallbackRoutes = modelProviders.fallbackRoutes(plan.route)
        if (fallbackRoutes.isEmpty()) {
            val result = AgentResult.Failure("No model provider is available")
            emit(AgentStepResult("answer", false, result.message), attempts)
            return AgentExecution(plan, steps, result, attempts, executionContext)
        }

        var routedPlan = plan
        var lastError: Throwable? = null
        for ((index, route) in fallbackRoutes.withIndex()) {
            val provider = modelProviders.get(route) ?: continue
            if (route != routedPlan.route) {
                routedPlan = routedPlan.copy(route = route)
                emit(AgentStepResult("fallback:${provider.id}", true, "Primary model unavailable; falling back to ${provider.id}"), attempts)
            }
            try {
                emit(AgentStepResult("model:${provider.id}", true, "Generating response"), attempts)
                val response = provider.generate(
                    ModelRequest(
                        prompt = task.input,
                        taskId = task.id,
                        metadata = task.metadata
                    )
                )
                val result = AgentResult.Success(response.text)
                emit(AgentStepResult("verify", true, "Response generated successfully"), attempts)
                emit(AgentStepResult("answer", true, result.text), attempts)
                return AgentExecution(routedPlan, steps, result, attempts, executionContext)
            } catch (error: Throwable) {
                lastError = error
                if (index < fallbackRoutes.lastIndex) {
                    emit(AgentStepResult("model:${provider.id}", false, "Provider failed; trying next available provider"), attempts)
                }
            }
        }

        val result = AgentResult.Failure(
            "All available model providers failed: ${lastError?.message ?: "unknown error"}",
            lastError
        )
        emit(AgentStepResult("answer", false, result.message), attempts)
        return AgentExecution(routedPlan, steps, result, attempts, executionContext)
    }

    private fun plannedToolIds(plan: AgentPlan): List<String> =
        if (plan.subtasks.size == 1) {
            listOfNotNull(plan.subtaskToolIds.firstOrNull() ?: plan.toolId)
        } else if (plan.subtasks.isNotEmpty()) {
            plan.subtaskToolIds.filterNotNull()
        } else {
            listOfNotNull(plan.toolId)
        }

    private fun planAndRoute(task: AgentTask): AgentPlan {
        val initialPlan = planner.plan(
            task = task,
            availableToolIds = toolRegistry.list().map { it.id }.toSet()
        )
        val routedPlan = initialPlan.copy(route = modelRouter.route(task, initialPlan))
        val provider = modelProviders.get(routedPlan.route)?.takeIf { it.isAvailable }
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
