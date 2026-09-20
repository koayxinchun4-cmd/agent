package com.example.agent.nexus.agent

data class AgentSession(
    val id: String,
    val task: AgentTask,
    val createdAt: Long = System.currentTimeMillis()
)