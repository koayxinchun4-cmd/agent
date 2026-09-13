package com.example.agent.nexus.agent

/** Immutable session identity used to correlate turns, approvals and observations. */
data class AgentSession(
    val id: String,
    val task: AgentTask,
    val state: AgentRuntimeState = AgentRuntimeState.CREATED,
    val turnIndex: Int = 0
) {
    fun advance(next: AgentRuntimeState): AgentSession = copy(
        state = next,
        turnIndex = turnIndex + 1
    )
}

/** One bounded model/tool cycle inside an AgentSession. */
data class AgentTurn(
    val index: Int,
    val plan: AgentPlan,
    val steps: List<AgentStepResult> = emptyList(),
    val observations: List<AgentObservation> = emptyList()
)
