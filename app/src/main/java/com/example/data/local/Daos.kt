package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM sessions WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSession?

    @Query("SELECT * FROM sessions WHERE isArchived = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: ChatSession)

    @Query("UPDATE sessions SET title = :title, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSession(session: ChatSession)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesListForSession(sessionId: String): List<ChatMessage>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesForSession(sessionId: String, limit: Int): List<ChatMessage>

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId")
    suspend fun getMessageCount(sessionId: String): Int
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY isBuiltIn DESC, name ASC")
    fun getAllSkills(): Flow<List<AgentSkill>>

    @Query("SELECT * FROM skills WHERE id = :skillId LIMIT 1")
    suspend fun getSkillById(skillId: String): AgentSkill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: AgentSkill)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSkills(skills: List<AgentSkill>)

    @Update
    suspend fun updateSkill(skill: AgentSkill)

    @Delete
    suspend fun deleteSkill(skill: AgentSkill)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY importance DESC, lastAccessedAt DESC")
    fun getAllMemories(): Flow<List<AgentMemory>>

    @Query("SELECT * FROM memories ORDER BY importance DESC, lastAccessedAt DESC")
    suspend fun getMemoriesList(): List<AgentMemory>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY importance DESC, lastAccessedAt DESC")
    fun getMemoriesByCategory(category: String): Flow<List<AgentMemory>>

    @Query("SELECT * FROM memories WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): AgentMemory?

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' OR `key` LIKE '%' || :query || '%'")
    fun searchMemories(query: String): Flow<List<AgentMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AgentMemory): Long

    @Update
    suspend fun updateMemory(memory: AgentMemory)

    @Query("UPDATE memories SET lastAccessedAt = :timestamp WHERE id = :id")
    suspend fun touchMemory(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMemory(memory: AgentMemory)

    @Query("DELETE FROM memories WHERE id = :memoryId")
    suspend fun deleteMemoryById(memoryId: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}
