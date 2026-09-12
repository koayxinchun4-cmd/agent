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

class UserPreferenceStoreTest {
    @Test
    fun setAndGetPreservesValueMetadataAndTimestamp() = runBlocking {
        val store = UserPreferenceStore(FakeAgentMemoryDao())
        val preference = UserPreference(
            key = "language",
            value = "zh-Hant",
            metadata = mapOf("source" to "user"),
            updatedAt = 42L
        )

        store.set(preference)

        assertEquals(preference, store.get("language"))
    }

    @Test
    fun settingSameKeyReturnsLatestPreference() = runBlocking {
        val store = UserPreferenceStore(FakeAgentMemoryDao())
        store.set(UserPreference("style", "concise", updatedAt = 1L))
        store.set(UserPreference("style", "detailed", updatedAt = 2L))

        assertEquals("detailed", store.get("style")?.value)
    }

    @Test
    fun listReturnsDistinctPreferences() = runBlocking {
        val store = UserPreferenceStore(FakeAgentMemoryDao())
        store.set(UserPreference("language", "zh-Hant", updatedAt = 1L))
        store.set(UserPreference("language", "en", updatedAt = 2L))
        store.set(UserPreference("style", "concise", updatedAt = 3L))

        assertEquals(
            listOf("style", "language"),
            store.list().map { it.key }
        )
    }

    @Test
    fun removeDeletesPreference() = runBlocking {
        val store = UserPreferenceStore(FakeAgentMemoryDao())
        store.set(UserPreference("language", "zh-Hant"))

        store.remove("language")

        assertNull(store.get("language"))
        assertTrue(store.list().isEmpty())
    }

    @Test
    fun blankKeyIsRejected() = runBlocking {
        val store = UserPreferenceStore(FakeAgentMemoryDao())

        val error = runCatching { store.get(" ") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
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
