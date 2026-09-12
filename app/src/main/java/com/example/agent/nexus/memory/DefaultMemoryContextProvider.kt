package com.example.agent.nexus.memory

/**
 * Empty memory provider for executions that do not have a configured memory source.
 */
class DefaultMemoryContextProvider : MemoryContextProvider {
    override suspend fun load(taskId: String?, projectId: String?): AgentMemoryContext =
        AgentMemoryContext()
}
