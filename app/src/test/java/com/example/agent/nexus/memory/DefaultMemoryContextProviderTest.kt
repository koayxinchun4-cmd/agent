package com.example.agent.nexus.memory

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultMemoryContextProviderTest {
    @Test
    fun returnsEmptyContextWithoutConfiguredStorage() = runBlocking {
        val context = DefaultMemoryContextProvider().load(
            taskId = "task-1",
            projectId = "project-1"
        )

        assertTrue(context.items.isEmpty())
    }
}
