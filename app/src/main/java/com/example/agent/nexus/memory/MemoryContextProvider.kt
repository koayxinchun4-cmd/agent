package com.example.agent.nexus.memory

/**
 * Supplies scoped memory to Agent Core without exposing storage details.
 */
interface MemoryContextProvider {
    suspend fun load(
        taskId: String? = null,
        projectId: String? = null
    ): AgentMemoryContext
}
