package com.example.agent.nexus.agent

/**
 * Lightweight progress event emitted while an Agent Task is executing.
 * The completed step snapshot lets the UI render the real execution timeline
 * without exposing the orchestrator itself as UI state.
 */
data class AgentProgress(
    val step: AgentStepResult,
    val attempt: Int = 0,
    val steps: List<AgentStepResult> = emptyList()
)
