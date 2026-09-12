package com.example.agent.nexus.memory

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentExecutionContextTest {
    @Test
    fun contextCarriesTaskProjectMemoryAndMetadata() {
        val context = AgentExecutionContext(
            taskId = "task-123",
            projectId = "project-1",
            memory = AgentMemoryContext(
                listOf(MemoryItem(MemoryScope.PROJECT, "language", "Kotlin"))
            ),
            metadata = mapOf("source" to "text")
        )

        assertEquals("task-123", context.taskId)
        assertEquals("project-1", context.projectId)
        assertEquals("Kotlin", context.memory.forScope(MemoryScope.PROJECT).single().value)
        assertEquals("text", context.metadata["source"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankTaskIdIsRejected() {
        AgentExecutionContext(taskId = " ")
    }
}
