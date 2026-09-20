package com.example.agent.nexus.agent

data class AgentLoopConfig(
    val maxAttempts: Int = 3,
    val maxFallbackRoutes: Int = 3,
    val enableRecovery: Boolean = true
)