package com.example.agent.nexus.memory

/**
 * Immutable context shared across one Agent execution.
 * Keeps task identity, scoped memory, and extensible metadata together without
 * coupling Agent Core to a concrete memory store or transport.
 */
data class AgentExecutionContext(
    val taskId: String,
    val projectId: String? = null,
    val memory: AgentMemoryContext = AgentMemoryContext(),
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
    }
}
