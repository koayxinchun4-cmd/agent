package com.example.agent.nexus.memory

/**
 * Project-scoped memory kept separate from conversation content and user preferences.
 * Metadata keeps the contract extensible as project context grows.
 */
data class ProjectMemory(
    val projectId: String,
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
