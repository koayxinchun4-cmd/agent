package com.nexus.ai.engine

/**
 * 所有 AI 引擎的通用介面，不論是 Gemini 還是 OpenRouter，
  * 只要實作這個介面，就能被 HybridAiRepository 統一呼叫。
   */
   interface BaseAiEngine {
        suspend fun generateResponse(prompt: String): Result<String>
   }

   // ==========================================
   // 以下是 OpenRouter API 所需的資料模型 (JSON 對應)
   // ==========================================

   data class OpenRouterRequest(
        val model: String,
            val messages: List<Message>
   )

   data class Message(
        val role: String, // 通常是 "user" 或 "assistant"
            val content: String
   )

   data class OpenRouterResponse(
        val choices: List<Choice>?
   )

   data class Choice(
        val message: Message?
   )
   
   )
   )
   )
   )
   }