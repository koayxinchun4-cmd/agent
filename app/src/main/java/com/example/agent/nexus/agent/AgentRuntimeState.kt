package com.example.agent.nexus.agent

/** Explicit lifecycle states for a long-running Nexus task. */
enum class AgentRuntimeState {
    CREATED,
    UNDERSTANDING,
    PLANNING,
    WAITING_FOR_PERMISSION,
    WAITING_FOR_CONFIRMATION,
    EXECUTING,
    VERIFYING,
    RETRYING,
    REPLANNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/** A state transition that can be rendered directly by the mobile timeline. */
data class AgentStateTransition(
    val from: AgentRuntimeState,
    val to: AgentRuntimeState,
    val reason: String? = null
)
