package com.example.agent.nexus.memory

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentMemoryContextTest {
    @Test
    fun contextCanFilterByMemoryScope() {
        val context = AgentMemoryContext(
            items = listOf(
                MemoryItem(MemoryScope.PROJECT, "language", "Kotlin"),
                MemoryItem(MemoryScope.TASK, "status", "running"),
                MemoryItem(MemoryScope.PROJECT, "ui", "Compose")
            )
        )

        assertEquals(2, context.forScope(MemoryScope.PROJECT).size)
        assertEquals(1, context.forScope(MemoryScope.TASK).size)
        assertTrue(context.forScope(MemoryScope.CONVERSATION).isEmpty())
    }

    @Test
    fun providerContractExposesContextWithoutStorageDetails() = runBlocking {
        val provider = object : MemoryContextProvider {
            override suspend fun load(taskId: String?, projectId: String?): AgentMemoryContext =
                AgentMemoryContext(
                    listOf(
                        MemoryItem(
                            scope = MemoryScope.TASK,
                            key = "task.id",
                            value = taskId.orEmpty()
                        )
                    )
                )
        }

        val context = provider.load(taskId = "task-123", projectId = "project-1")

        assertEquals("task-123", context.forScope(MemoryScope.TASK).single().value)
    }
}
