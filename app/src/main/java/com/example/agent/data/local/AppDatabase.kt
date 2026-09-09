package com.example.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ChatMessage::class, AgentMemory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun agentMemoryDao(): AgentMemoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS agent_memory (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, key TEXT NOT NULL, value TEXT NOT NULL, timestamp INTEGER NOT NULL)"
                )
            }
        }
    }
}
