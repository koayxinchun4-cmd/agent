package com.example.agent.nexus.memory

/**
 * Read-only memory snapshot supplied to Agent Core for one execution context.
 */
data class AgentMemoryContext(
    val items: List<MemoryItem> = emptyList()
) {
    fun forScope(scope: MemoryScope): List<MemoryItem> =
        items.filter { it.scope == scope }
}
