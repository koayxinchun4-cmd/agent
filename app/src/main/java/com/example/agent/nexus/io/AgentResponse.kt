package com.example.agent.nexus.io

/** A channel-neutral response emitted by Agent Core. */
data class AgentResponse(
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
