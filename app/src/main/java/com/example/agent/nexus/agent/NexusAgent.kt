package com.example.agent.nexus.agent

import com.example.agent.nexus.memory.AgentExecutionContext
import com.example.agent.nexus.memory.DefaultMemoryContextProvider
import com.example.agent.nexus.memory.MemoryContextProvider
import com.example.agent.nexus.tool.DefaultToolRuntime
import com.example.agent.nexus.tool.RiskLevel
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult
import com.example.agent.nexus.tool.ToolRuntime
import com.example.agent.nexus.tool.ToolRuntimeResult

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
    private val memoryContextProvider: MemoryContextProvider = DefaultMemoryContextProvider(),
    private val toolRuntime: ToolRuntime = DefaultToolRuntime()
) {
    suspend fun execute(task: AgentTask): AgentResult =
        executeDetailed(task).result

    /**
     * Preflight the planned tool before execution so the UI can request explicit
     * user confirmation without invoking the tool first.
     */
    fun previewConfirmation(task: AgentTask): AgentConfirmationRequest? {
        if (task.metadata[AgentTask.CONFIRMATION_GRANTED] == "true") return null
        val plan = planAndRoute(task)
        val toolId = plan.toolId ?: return null
        val tool = toolRegistry.get(toolId) ?: return null
        return tool.takeIf { it.riskLevel == RiskLevel.REQUIRES_CONFIRMATION }
            ?.let { AgentConfirmationRequest(task = task, tool = it, plan = plan) }
    }

    suspend fun executeDetailed(
        task: AgentTask,
        onProgress: (AgentProgress) -> Unit = {}
    ): AgentExecution {
        var currentTask = task
        val projectId = task.metadata[AgentTask.PROJECT_ID]
        val session = AgentSession(id = task.id, task = task)
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
        val contextStore = AgentContextStore()

        fun emit(step: AgentStepResult, attempt: Int = 0) {
            steps += step
            contextStore.add(AgentObservation("execution", step.output, step.success))
            onProgress(AgentProgress(step = step, attempt = attempt, steps = steps.toList()))
        }

        emit(AgentStepResult("understand_request", true, "Request understood"))
        emit(AgentStepResult("session", true, "Agent session ${session.id} created"))
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
                    emit(AgentStepResult("fallback:${provider.id}", true, "Primary model unavailable; falling back to ${provider.id}"))
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
                        emit(AgentStepResult("model:${provider.id}", false, "Provider failed; trying next available provider"))
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

        // Subtask-aware execution: when the plan decomposes the task into
        // multiple subtasks with different tool assignments, execute each
        // subtask with its assigned tool in order and combine results.
        val hasMultipleSubtaskTools = plan.subtasks.size > 1 &&
            plan.subtaskToolIds.any { it != null } &&
            plan.subtaskToolIds.filterNotNull().distinct().size > 1

        if (hasMultipleSubtaskTools) {
            return executeSubtasks(
                plan, currentTask, session, steps, executionContext, onProgress
            )
        }

        val tool = toolRegistry.get(toolId)
        if (tool == null) {
            val result = AgentResult.Failure("Nexus 找不到工具：$toolId")
            emit(AgentStepResult("answer", false, result.message))
            return AgentExecution(plan, steps, result, context = executionContext)
        }

        var lastResult: ToolResult = ToolResult.Failure("Tool has not executed")
        var attempts = 0

        while (attempts < loopConfig.maxAttempts) {
            attempts += 1
            val attemptStep = "use_tool:$toolId#attempt$attempts"
            val runtimeResult = toolRuntime.execute(
                sessionId = session.id,
                task = currentTask,
                tool = tool
            )
            lastResult = when (runtimeResult) {
                is ToolRuntimeResult.Success -> runtimeResult.result
                is ToolRuntimeResult.Denied -> ToolResult.Failure(runtimeResult.message)
            }
            emit(lastResult.toAgentStep(attemptStep), attempts)

            when (val verification = verifier.verify(lastResult)) {
                VerificationResult.Passed -> {
                    val result = AgentResult.Success(
                        "${(lastResult as ToolResult.Success).text}\n\n[Model route: ${plan.route}]"
                    )
                    contextStore.add(AgentObservation("verification", "Verification passed"))
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

                    emit(AgentStepResult("replanning", true, "Re-planning from failure observations"), attempts)
                    currentTask = recoveryPolicy.adjust(currentTask, diagnosis, attempts)
                    plan = planAndRoute(currentTask)
                    emit(AgentStepResult("adjust_plan", true, "Adjusted input for recovery attempt ${attempts + 1}"), attempts)
                    emit(AgentStepResult("verify", false, "Recovery prepared; retrying"), attempts)
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

    private suspend fun executeSubtasks(
        plan: AgentPlan,
        task: AgentTask,
        session: AgentSession,
        steps: MutableList<AgentStepResult>,
        executionContext: AgentExecutionContext,
        onProgress: (AgentProgress) -> Unit
    ): AgentExecution {
        val results = mutableListOf<String>()
        val failures = mutableListOf<String>()

        fun emitStep(step: AgentStepResult) {
            steps += step
            onProgress(AgentProgress(step = step, attempt = 1, steps = steps.toList()))
        }

        plan.subtasks.forEachIndexed { index, subtaskText ->
            val subtaskToolId = plan.subtaskToolIds.getOrNull(index)
            val subtaskLabel = "subtask:${index + 1}"

            if (subtaskToolId == null) {
                emitStep(AgentStepResult(subtaskLabel, true, "No tool needed: $subtaskText"))
                results.add(subtaskText)
                return@forEachIndexed
            }

            val tool = toolRegistry.get(subtaskToolId)
            if (tool == null) {
                val msg = "Tool not found: $subtaskToolId"
                emitStep(AgentStepResult(subtaskLabel, false, msg))
                failures.add("$subtaskLabel: $msg")
                return@forEachIndexed
            }

            val subtask = AgentTask(
                id = "${task.id}-sub${index + 1}",
                input = subtaskText,
                metadata = task.metadata
            )

            emitStep(AgentStepResult("use_tool:$subtaskToolId#$subtaskLabel", true, "Executing: $subtaskText"))

            val runtimeResult = toolRuntime.execute(
                sessionId = session.id,
                task = subtask,
                tool = tool
            )

            when (val toolResult = when (runtimeResult) {
                is ToolRuntimeResult.Success -> runtimeResult.result
                is ToolRuntimeResult.Denied -> ToolResult.Failure(runtimeResult.message)
            }) {
                is ToolResult.Success -> {
                    val verified = verifier.verify(toolResult)
                    when (verified) {
                        is VerificationResult.Passed -> {
                            emitStep(AgentStepResult(subtaskLabel, true, toolResult.text))
                            results.add(toolResult.text)
                        }
                        is VerificationResult.Retry -> {
                            emitStep(AgentStepResult(subtaskLabel, false, "Subtask failed verification: ${verified.reason}"))
                            failures.add("$subtaskLabel: ${verified.reason}")
                        }
                    }
                }
                is ToolResult.Failure -> {
                    emitStep(AgentStepResult(subtaskLabel, false, toolResult.message))
                    failures.add("$subtaskLabel: ${toolResult.message}")
                }
            }
        }

        val combinedText = results.joinToString("\n\n")
        val finalResult = if (failures.isEmpty()) {
            emitStep(AgentStepResult("verify", true, "All subtasks completed successfully"))
            AgentResult.Success("$combinedText\n\n[Model route: ${plan.route}; subtasks: ${plan.subtasks.size}]")
        } else {
            val allOutput = listOfNotNull(
                combinedText.takeIf { it.isNotBlank() },
                "Failures:\n${failures.joinToString("\n")}"
            ).joinToString("\n\n")
            emitStep(AgentStepResult("verify", false, "${failures.size} subtask(s) failed"))
            AgentResult.Failure(allOutput)
        }

        emitStep(AgentStepResult("answer", failures.isEmpty(), finalResult.let {
            when (it) {
                is AgentResult.Success -> it.text
                is AgentResult.Failure -> it.message
            }
        }))

        return AgentExecution(
            plan = plan,
            steps = steps.toList(),
            result = finalResult,
            attempts = 1,
            context = executionContext
        )
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
        val tool = toolRegistry.get(toolId)
            ?: return AgentResult.Failure("Nexus 找不到工具：$toolId")
        return when (val result = toolRuntime.execute(task.id, task, tool)) {
            is ToolRuntimeResult.Success -> when (val toolResult = result.result) {
                is ToolResult.Success -> AgentResult.Success(toolResult.text)
                is ToolResult.Failure -> AgentResult.Failure(toolResult.message, toolResult.cause)
            }
            is ToolRuntimeResult.Denied -> AgentResult.Failure(result.message)
        }
    }
}
