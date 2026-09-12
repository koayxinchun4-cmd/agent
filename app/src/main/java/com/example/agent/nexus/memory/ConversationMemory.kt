package com.example.agent.nexus.memory

/**
 * Local-first conversation context boundary. Implementations can later be backed
 * by Room, another local store, or a sync layer without changing Agent Core.
 */
interface ConversationMemory {
    suspend fun append(entry: ConversationMemoryEntry)

    suspend fun recent(limit: Int = DEFAULT_RECENT_LIMIT): List<ConversationMemoryEntry>

    suspend fun clear()

    companion object {
        const val DEFAULT_RECENT_LIMIT = 20
    }
}

data class ConversationMemoryEntry(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)
