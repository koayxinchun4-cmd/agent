package com.example.agent.nexus.agent

/**
 * Lightweight progress event emitted while an Agent Task is executing.
 * This keeps the UI responsive without exposing orchestration internals as UI state.
 */
data class AgentProgress(
    val step: AgentStepResult,
    val attempt: Int = 0
)
