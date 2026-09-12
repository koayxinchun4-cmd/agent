package com.example.agent.nexus.memory

import com.example.agent.data.local.AgentMemory
import com.example.agent.data.local.AgentMemoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMemoryStoreTest {
    @Test
    fun setAndGetPreservesTaskMemory() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())
        val memory = TaskMemory(
            taskId = "task-1",
            key = "plan",
            value = "inspect repository",
            metadata = mapOf("source" to "planner"),
            updatedAt = 42L
        )

        store.set(memory)

        assertEquals(memory, store.get("task-1", "plan"))
    }

    @Test
    fun sameKeyReturnsLatestMemory() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())
        store.set(TaskMemory("task-1", "status", "running", updatedAt = 1L))
        store.set(TaskMemory("task-1", "status", "done", updatedAt = 2L))

        assertEquals("done", store.get("task-1", "status")?.value)
    }

    @Test
    fun taskMemoryIsIsolatedByTaskId() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())
        store.set(TaskMemory("task-a", "status", "A"))
        store.set(TaskMemory("task-b", "status", "B"))

        assertEquals("A", store.get("task-a", "status")?.value)
        assertEquals("B", store.get("task-b", "status")?.value)
        assertEquals(listOf("A"), store.list("task-a").map { it.value })
    }

    @Test
    fun listReturnsLatestValueForEachKey() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())
        store.set(TaskMemory("task-1", "status", "running", updatedAt = 1L))
        store.set(TaskMemory("task-1", "status", "done", updatedAt = 2L))
        store.set(TaskMemory("task-1", "result", "verified", updatedAt = 3L))

        assertEquals(
            listOf("result", "status"),
            store.list("task-1").map { it.key }
        )
        assertEquals("done", store.list("task-1").first { it.key == "status" }.value)
    }

    @Test
    fun removeDeletesOnlySelectedTaskMemory() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())
        store.set(TaskMemory("task-1", "status", "done"))
        store.set(TaskMemory("task-2", "status", "keep"))

        store.remove("task-1", "status")

        assertNull(store.get("task-1", "status"))
        assertEquals("keep", store.get("task-2", "status")?.value)
    }

    @Test
    fun blankTaskIdOrKeyIsRejected() = runBlocking {
        val store = TaskMemoryStore(FakeAgentMemoryDao())

        assertTrue(runCatching { store.get(" ", "status") }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { store.get("task-1", " ") }.exceptionOrNull() is IllegalArgumentException)
    }

    private class FakeAgentMemoryDao : AgentMemoryDao {
        private val memories = mutableListOf<AgentMemory>()

        override suspend fun insert(memory: AgentMemory) {
            memories += memory.copy(id = (memories.size + 1).toLong())
        }

        override fun observeAll(): Flow<List<AgentMemory>> = flowOf(memories)

        override suspend fun getRecent(): List<AgentMemory> =
            memories.sortedByDescending { it.timestamp }.take(50)

        override suspend fun findLatest(key: String): AgentMemory? =
            memories.filter { it.key == key }.maxByOrNull { it.timestamp }

        override suspend fun findByKeyPrefix(prefix: String): List<AgentMemory> =
            memories.filter { it.key.startsWith(prefix) }.sortedByDescending { it.timestamp }

        override suspend fun search(query: String): List<AgentMemory> =
            memories.filter { it.key.contains(query) || it.value.contains(query) }
                .sortedByDescending { it.timestamp }
                .take(20)

        override suspend fun deleteByKey(key: String) {
            memories.removeAll { it.key == key }
        }

        override suspend fun clearAll() {
            memories.clear()
        }
    }
}
