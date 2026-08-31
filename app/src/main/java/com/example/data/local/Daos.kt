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
    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: ChatSession)

    @Delete
    suspend fun deleteSession(session: ChatSession)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesListForSession(sessionId: String): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

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
    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<AgentMemory>>

    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    suspend fun getMemoriesList(): List<AgentMemory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AgentMemory)

    @Delete
    suspend fun deleteMemory(memory: AgentMemory)

    @Query("DELETE FROM memories WHERE id = :memoryId")
    suspend fun deleteMemoryById(memoryId: Long)
}
