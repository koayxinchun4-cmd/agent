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

class ProjectMemoryStoreTest {
    @Test
    fun setAndGetPreservesProjectMemory() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())
        val memory = ProjectMemory(
            projectId = "nexus",
            key = "architecture",
            value = "agent core",
            metadata = mapOf("source" to "user"),
            updatedAt = 42L
        )

        store.set(memory)

        assertEquals(memory, store.get("nexus", "architecture"))
    }

    @Test
    fun sameKeyReturnsLatestMemory() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())
        store.set(ProjectMemory("nexus", "status", "draft", updatedAt = 1L))
        store.set(ProjectMemory("nexus", "status", "ready", updatedAt = 2L))

        assertEquals("ready", store.get("nexus", "status")?.value)
    }

    @Test
    fun projectMemoryIsIsolatedByProjectId() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())
        store.set(ProjectMemory("project-a", "status", "A"))
        store.set(ProjectMemory("project-b", "status", "B"))

        assertEquals("A", store.get("project-a", "status")?.value)
        assertEquals("B", store.get("project-b", "status")?.value)
        assertEquals(listOf("A"), store.list("project-a").map { it.value })
    }

    @Test
    fun listReturnsLatestValueForEachKey() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())
        store.set(ProjectMemory("nexus", "status", "draft", updatedAt = 1L))
        store.set(ProjectMemory("nexus", "status", "ready", updatedAt = 2L))
        store.set(ProjectMemory("nexus", "owner", "cc", updatedAt = 3L))

        assertEquals(
            listOf("owner", "status"),
            store.list("nexus").map { it.key }
        )
        assertEquals("ready", store.list("nexus").first { it.key == "status" }.value)
    }

    @Test
    fun removeDeletesOnlySelectedProjectMemory() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())
        store.set(ProjectMemory("nexus", "status", "ready"))
        store.set(ProjectMemory("other", "status", "keep"))

        store.remove("nexus", "status")

        assertNull(store.get("nexus", "status"))
        assertEquals("keep", store.get("other", "status")?.value)
    }

    @Test
    fun blankProjectIdOrKeyIsRejected() = runBlocking {
        val store = ProjectMemoryStore(FakeAgentMemoryDao())

        assertTrue(runCatching { store.get(" ", "status") }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { store.get("nexus", " ") }.exceptionOrNull() is IllegalArgumentException)
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
