package com.example.agent.data.repository

import com.example.agent.data.local.ChatDao
import com.example.agent.data.local.ChatMessage
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Content
import com.example.agent.data.remote.Part
import com.example.agent.BuildConfig
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService
) {
    fun getAllMessages(): Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun sendMessage(userText: String): String {
        chatDao.insertMessage(ChatMessage(content = userText, isUser = true))

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = userText))))
        )
        val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
        val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "No response"

        chatDao.insertMessage(ChatMessage(content = replyText, isUser = false))
        return replyText
    }

    suspend fun saveAssistantMessage(text: String) {
        chatDao.insertMessage(ChatMessage(content = text, isUser = false))
    }

    suspend fun clearHistory() = chatDao.clearAll()
}
