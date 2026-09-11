package com.example.agent.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
)

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>? = null
)

data class OpenRouterChoice(
    val message: OpenRouterMessage? = null
)
