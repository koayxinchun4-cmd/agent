package com.nexus.ai.engine // 宣告此檔案隸屬的套件路徑

import com.google.ai.client.generativeai.GenerativeModel // 引用 Google 官方 Gemini SDK 核心類別

/**
 * Gemini 引擎：直接調用 Google 官方 Generative AI SDK
  */
  class GeminiEngine(
        private val apiKey: String, // 傳入 Google AI Studio 申請的 API Key
            private val modelName: String = "gemini-1.5-flash" // 預設使用速度快且免費額度高的 Flash 模型
  ) : BaseAiEngine { // 實作通用 BaseAiEngine 介面
  
      // 懶載入建立 GenerativeModel 物件 (使用時才初始化)
          private val generativeModel by lazy {
                    GenerativeModel(
                                    modelName = modelName, // 指定模型名稱
                                                apiKey = apiKey       // 設定 API 密鑰
                    )
          }

              // 實作介面規定的 generateResponse 方法
                  override suspend fun generateResponse(prompt: String): Result<String> {
                            return try {
                                            // 發送 Prompt 給 Google Gemini API 並等待生成回應
                                                        val response = generativeModel.generateContent(prompt)
                                                                    val text = response.text // 提取生成出來的文字內容

                                                                                if (!text.isNullOrEmpty()) {
                                                                                                    Result.success(text) // 成功取得文字，回傳 Result.success
                                                                                } else {
                                                                                                    Result.failure(Exception("Gemini 回應內容為空")) // 文字內容空白時處理
                                                                                }
                            } catch (e: Exception) {
                                            // 捕捉 API Key 錯誤、網路問題或配額限制異常
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
          }}
  )