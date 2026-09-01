package com.nexus.ai.engine.remote

import com.nexus.ai.engine.OpenRouterRequest
import com.nexus.ai.engine.OpenRouterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit 介面：定義與 OpenRouter REST API 通訊的 Endpoint
  */
  interface OpenRouterApi {
        @POST("api/v1/chat/completions")
            suspend fun chatCompletion(
                        @Header("Authorization") apiKey: String,
                                @Header("HTTP-Referer") referer: String = "https://github.com/koayxinchun4-cmd/agent",
                                        @Header("X-Title") title: String = "Nexus AI Agent",
                                                @Body request: OpenRouterRequest
            ): Response<OpenRouterResponse>
  }
  
            )
  }