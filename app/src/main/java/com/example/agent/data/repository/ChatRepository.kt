package com.example.agent.data.repository

import com.example.agent.BuildConfig
import com.example.agent.data.local.ChatDao
import com.example.agent.data.local.ChatMessage
import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.Part
import kotlinx.coroutines.flow.Flow

private val NEXUS_SYSTEM_PROMPT = """
你是 Nexus 智能助手，一个原生 Android 手机上的 AI Agent。
你的定位不是单纯聊天机器人，而是用户的手机 Team Leader：理解目标、拆解任务、选择合适技能，并在获得必要授权后协助执行。
核心约束：你以手机为主要执行环境，不要求电脑，不要求 Root。涉及文件、App、通知、浏览器或其他系统能力时，只能建议或使用 Android 正规授权机制，不绕过系统安全限制。
你服务的用户可能来自马来西亚，因此自然支持简体中文、繁体中文、Bahasa Melayu 和 English，也允许混合语言。
目前可用能力以聊天和本地历史为主；不要假装已经执行尚未接入的工具。对于 Web Research、手机文件、App Agent、Skills、Memory、AI Moments、Office、GitHub/Codex、语音和 AI 朋友圈等能力，要明确区分“已实现”和“规划中”。
回答时优先给出清晰、可执行的下一步；如果任务需要用户确认或系统权限，先说明。
""".trimIndent()

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
            systemInstruction = Content(parts = listOf(Part(text = NEXUS_SYSTEM_PROMPT))),
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
