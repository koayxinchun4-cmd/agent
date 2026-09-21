package com.example.agent.nexus.agent

/**
 * Single agent execution turn (one user input → intent → plan → execute → result).
 * Minimal definition so AgentCheckpoint compiles; expand later as needed.
 */
data class AgentTurn(
    val id: String,
    val userInput: String,
    val intent: AgentIntent,
    val result: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
