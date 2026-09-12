package com.example.agent.nexus.memory

import com.example.agent.data.local.ChatDao
import com.example.agent.data.local.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationMemoryTest {
    @Test
    fun roomMemoryAppendsAndReadsRecentEntries() = runBlocking {
        val dao = FakeChatDao()
        val memory = RoomConversationMemory(dao)

        memory.append(ConversationMemoryEntry("hello", true, 1L))
        memory.append(ConversationMemoryEntry("hi", false, 2L))

        assertEquals(
            listOf(
                ConversationMemoryEntry("hi", false, 2L),
                ConversationMemoryEntry("hello", true, 1L)
            ),
            memory.recent(2)
        )
    }

    @Test
    fun recentRejectsNonPositiveLimit() = runBlocking {
        val memory = RoomConversationMemory(FakeChatDao())

        val error = runCatching { memory.recent(0) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun clearDelegatesToLocalStore() = runBlocking {
        val dao = FakeChatDao()
        val memory = RoomConversationMemory(dao)
        memory.append(ConversationMemoryEntry("hello", true, 1L))

        memory.clear()

        assertTrue(dao.messages.isEmpty())
    }

    private class FakeChatDao : ChatDao {
        val messages = mutableListOf<ChatMessage>()

        override suspend fun insertMessage(message: ChatMessage) {
            messages += message.copy(id = (messages.size + 1).toLong())
        }

        override fun getAllMessages(): Flow<List<ChatMessage>> = flowOf(messages)

        override suspend fun getAllMessagesOnce(): List<ChatMessage> = messages.toList()

        override suspend fun getRecentMessages(limit: Int): List<ChatMessage> =
            messages.sortedByDescending { it.timestamp }.take(limit)

        override suspend fun clearAll() {
            messages.clear()
        }
    }
}
