package com.example.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChatMessage::class, AgentMemory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun memoryDao(): AgentMemoryDao
}
