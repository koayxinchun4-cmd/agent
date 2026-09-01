package com.nexus.ai.engine // 宣告此檔案隸屬的套件路徑

import com.nexus.ai.engine.remote.OpenRouterApi // 引用先前建立的 OpenRouterApi 介面
import okhttp3.OkHttpClient // 引用 HTTP 連線客戶端元件
import okhttp3.logging.HttpLoggingInterceptor // 引用網路請求日誌攔截器
import retrofit2.Retrofit // 引用 API 連線核心庫 Retrofit
import retrofit2.converter.gson.GsonConverterFactory // 引用 JSON 自動解析轉換器

/**
 * OpenRouter 引擎：負責透過網路 API 發送 Prompt 並取得 AI 回答
  */
  class OpenRouterEngine(
        private val apiKey: String, // 傳入 API 金鑰 (Key)
            private val defaultModel: String = "openrouter/auto" // 預設呼叫的模型名稱 (免費自動分配)
  ) : BaseAiEngine { // 實作通用 BaseAiEngine 介面
  
      // 懶載入建立 Retrofit API 實例 (第一次用到時才會初始化)
          private val api: OpenRouterApi by lazy {
                    // 設定網路日誌紀錄器，可以在 Logcat 中看到發送與收到的詳細封包
                            val logging = HttpLoggingInterceptor().apply {
                                            level = HttpLoggingInterceptor.Level.BODY // 紀錄完整的 Body 資訊
                            }
                                    // 打造 OkHttpClient 並加入日誌攔截器
                                            val client = OkHttpClient.Builder()
                                                        .addInterceptor(logging)
                                                                    .build()

                                                                            // 打造 Retrofit 網路服務實例
                                                                                    Retrofit.Builder()
                                                                                                .baseUrl("https://openrouter.ai/") // 設定 OpenRouter 官方伺服器基底網址
                                                                                                            .client(client) // 使用剛才設定好的 OkHttp 客戶端
                                                                                                                        .addConverterFactory(GsonConverterFactory.create()) // 自動將接收到的 JSON 轉成 Kotlin 物件
                                                                                                                                    .build()
                                                                                                                                                .create(OpenRouterApi::class.java) // 綁定 API 介面
          }

              // 實作介面規定的 generateResponse 方法
                  override suspend fun generateResponse(prompt: String): Result<String> {
                            return try {
                                            // 封裝發送給 API 的請求資料結構
                                                        val request = OpenRouterRequest(
                                                                            model = defaultModel, // 使用指定的模型
                                                                                            messages = listOf(Message(role = "user", content = prompt)) // 組合使用者輸入的訊息
                                                        )
                                                                    
                                                                                // 處理標頭 Authorization 格式，確保帶有 "Bearer " 字首
                                                                                            val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                                                                                                        
                                                                                                                    // 發送異步網路請求並等待回應
                                                                                                                                val response = api.chatCompletion(apiKey = authHeader, request = request)

                                                                                                                                            // 判斷伺服器是否成功回應 200 OK
                                                                                                                                                        if (response.isSuccessful) {
                                                                                                                                                                            // 提取 AI 回答的文字內容
                                                                                                                                                                                            val content = response.body()?.choices?.firstOrNull()?.message?.content
                                                                                                                                                                                                            if (!content.isNullOrEmpty()) {
                                                                                                                                                                                                                                    Result.success(content) // 成功提取內容，回傳 Result.success
                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                    Result.failure(Exception("OpenRouter 回應內容為空")) // 文字內容為空時報錯
                                                                                                                                                                                                            }
                                                                                                                                                        } else {
                                                                                                                                                                            // 伺服器回傳 4xx 或 5xx 錯誤碼時處置
                                                                                                                                                                                            Result.failure(Exception("OpenRouter 請求失敗: HTTP ${response.code()} - ${response.errorBody()?.string()}"))
                                                                                                                                                        }
                            } catch (e: Exception) {
                                            // 捕捉網路斷線、超時或內部異常
                                                        Result.failure(e)
                            }
                  }
  }
  
                            }
                                                                                                                                                        }
                                                                                                                                                                                                            }
                                                                                                                                                                                                            }
                                                                                                                                                        }
                                                        )
                            }
                  }
                            }
          }}
  )