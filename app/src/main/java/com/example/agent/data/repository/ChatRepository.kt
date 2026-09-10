package com.example.agent.data.repository

import com.example.agent.BuildConfig
import com.example.agent.data.local.ChatDao
import com.example.agent.data.local.ChatMessage
import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Part
import com.example.agent.nexus.agent.NexusSystemPrompt
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService
) {
    fun getAllMessages(): Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun sendMessage(userText: String): String {
        chatDao.insertMessage(ChatMessage(content = userText, isUser = true))

        val history = chatDao.getAllMessagesOnce()
            .filter { it.content.isNotBlank() }
            .takeLast(40)
            .map { message ->
                Content(
                    role = if (message.isUser) "user" else "model",
                    parts = listOf(Part(text = message.content))
                )
            }

        val request = GeminiRequest(
            systemInstruction = Content(parts = listOf(Part(text = NexusSystemPrompt.text))),
            contents = history.ifEmpty {
                listOf(Content(role = "user", parts = listOf(Part(text = userText))))
            }
        )

        val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
        val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Nexus 没有收到可显示的回复。"

        chatDao.insertMessage(ChatMessage(content = replyText, isUser = false))
        return replyText
    }

    suspend fun saveAssistantMessage(text: String) {
        chatDao.insertMessage(ChatMessage(content = text, isUser = false))
    }

    suspend fun clearHistory() = chatDao.clearAll()
}
