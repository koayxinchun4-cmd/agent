package com.example.agent.nexus.agent

/** Final outcome of an Agent task. */
sealed interface AgentResult {
    data class Success(val text: String) : AgentResult
    data class Failure(val message: String, val cause: Throwable? = null) : AgentResult
}
