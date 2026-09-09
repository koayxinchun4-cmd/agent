package com.example.agent.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentMemoryDao {
    @Insert
    suspend fun insert(memory: AgentMemory)

    @Query("SELECT * FROM agent_memory ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AgentMemory>>

    @Query("SELECT * FROM agent_memory ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecent(): List<AgentMemory>

    @Query("SELECT * FROM agent_memory WHERE key = :key ORDER BY timestamp DESC LIMIT 1")
    suspend fun findLatest(key: String): AgentMemory?

    @Query("SELECT * FROM agent_memory WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 20")
    suspend fun search(query: String): List<AgentMemory>

    @Query("DELETE FROM agent_memory")
    suspend fun clearAll()
}
