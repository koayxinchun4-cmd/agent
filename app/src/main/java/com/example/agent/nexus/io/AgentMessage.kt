package com.example.agent.nexus.io

/** A channel-neutral message delivered to Agent Core. */
data class AgentMessage(
    val content: String,
    val sourceId: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
