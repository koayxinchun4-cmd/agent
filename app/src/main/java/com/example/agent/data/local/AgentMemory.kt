package com.example.agent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_memory")
data class AgentMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
