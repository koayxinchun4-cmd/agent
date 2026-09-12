package com.example.agent.nexus.memory

import com.example.agent.data.local.ChatDao
import com.example.agent.data.local.ChatMessage

/** Room-backed local implementation of the conversation memory contract. */
class RoomConversationMemory(
    private val chatDao: ChatDao
) : ConversationMemory {
    override suspend fun append(entry: ConversationMemoryEntry) {
        chatDao.insertMessage(
            ChatMessage(
                content = entry.content,
                isUser = entry.isUser,
                timestamp = entry.timestamp
            )
        )
    }

    override suspend fun recent(limit: Int): List<ConversationMemoryEntry> {
        require(limit > 0) { "memory limit must be positive" }
        return chatDao.getRecentMessages(limit).map { it.toMemoryEntry() }
    }

    override suspend fun clear() {
        chatDao.clearAll()
    }

    private fun ChatMessage.toMemoryEntry() = ConversationMemoryEntry(
        content = content,
        isUser = isUser,
        timestamp = timestamp
    )
}
