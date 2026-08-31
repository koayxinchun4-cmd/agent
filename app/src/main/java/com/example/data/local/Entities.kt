package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val activeSkillId: String? = null,
    val systemPrompt: String? = null,
    val messageCount: Int = 0
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "model" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val skillNameUsed: String? = null,
    val actionType: String? = null, // "office", "code", "auto", "memory", "none"
    val tokenCount: Int? = 0,
    val error: Boolean = false
)

@Entity(tableName = "skills")
data class AgentSkill(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val category: String, // "office", "automation", "coding", "productivity", "custom"
    val systemPrompt: String,
    val samplePromptsJson: String, // JSON array of suggestions
    val isBuiltIn: Boolean = true,
    val enabled: Boolean = true
)

@Entity(tableName = "memories")
data class AgentMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val content: String,
    val category: String = "general", // "preference", "device", "user_profile", "custom"
    val createdAt: Long = System.currentTimeMillis()
)
