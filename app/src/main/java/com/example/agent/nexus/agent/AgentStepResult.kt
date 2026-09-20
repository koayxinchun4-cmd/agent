package com.example.agent.nexus.agent

data class AgentStepResult(
    val step: String,
    val success: Boolean,
    val output: String
)