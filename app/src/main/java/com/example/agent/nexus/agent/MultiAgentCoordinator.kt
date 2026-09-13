package com.example.agent.nexus.agent

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/** A specialist agent owns one bounded capability domain. */
interface SpecialistAgent {
    val id: String
    val capabilities: Set<AgentIntent>
    suspend fun execute(task: AgentTask): AgentResult
}

/** Deterministic delegation layer for future parallel/multi-agent workflows. */
class MultiAgentCoordinator(
    private val specialists: List<SpecialistAgent>
) {
    suspend fun execute(task: AgentTask): AgentResult {
        coroutineContext.ensureActive()
        val intent = IntentClassifier().classify(task.input)
        val specialist = specialists.firstOrNull { intent in it.capabilities }
            ?: return AgentResult.Failure("No specialist agent is registered for $intent")
        return specialist.execute(task)
    }
}

/** Minimal failure-aware re-planning contract; implementations can use model or rules. */
interface AgentReplanner {
    fun replan(
        task: AgentTask,
        failedPlan: AgentPlan,
        failure: AgentObservation
    ): AgentPlan?
}

class ConservativeReplanner : AgentReplanner {
    override fun replan(
        task: AgentTask,
        failedPlan: AgentPlan,
        failure: AgentObservation
    ): AgentPlan? = when {
        failure.success -> null
        failedPlan.subtasks.size <= 1 -> null
        else -> failedPlan.copy(
            steps = failedPlan.steps + "recover_from:${failure.source}"
        )
    }
}
