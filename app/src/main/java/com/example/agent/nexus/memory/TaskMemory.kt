package com.example.agent.nexus.memory

/**
 * Task-scoped memory kept separate from conversation content, user preferences,
 * and project context.
 *
 * Metadata keeps the contract extensible as task context grows.
 */
data class TaskMemory(
    val taskId: String,
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
