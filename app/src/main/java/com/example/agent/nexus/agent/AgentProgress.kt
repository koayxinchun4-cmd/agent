package com.example.agent.nexus.agent

data class AgentProgress(
    val step: AgentStepResult,
    val attempt: Int,
    val steps: List<AgentStepResult>
)