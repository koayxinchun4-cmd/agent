package com.nexus.ai.engine // 宣告此檔案隸屬的套件路徑

import kotlinx.coroutines.Dispatchers // 引用協程調度器
import kotlinx.coroutines.withContext // 引用切換執行緒上下文的方法

/**
 * 混合 AI 儲存庫 (Repository)：
  * 負責整合與協調所有 AI 引擎，提供自動備援 (Failover) 機制。
   */
   class HybridAiRepository(
        private val primaryEngine: BaseAiEngine,   // 主引擎 (例如 GeminiEngine)
            private val fallbackEngine: BaseAiEngine? = null // 備援引擎 (例如 OpenRouterEngine，可選)
   ) {

        /**
             * 發送 Prompt 並取得回答 (包含自動切換備援邏輯)
                  * @param prompt 使用者輸入的提示詞
                       * @return Result<String> 回傳最終成功的文字或失敗例外
                            */
                                suspend fun getCompletion(prompt: String): Result<String> = withContext(Dispatchers.IO) { // 切換至背景 I/O 執行緒進行網路請求
                                        // 1. 優先嘗試使用主引擎發送請求
                                                val primaryResult = primaryEngine.generateResponse(prompt)
                                                
                                                        // 2. 如果主引擎執行成功，直接返回結果
                                                                if (primaryResult.isSuccess) {
                                                                                return@withContext primaryResult
                                                                }

                                                                        // 3. 若主引擎失敗，且有設定備援引擎，則自動啟動備援機制
                                                                                if (fallbackEngine != null) {
                                                                                                println("主 AI 引擎請求失敗: ${primaryResult.exceptionOrNull()?.message}，正在切換至備援引擎...")
                                                                                                            return@withContext fallbackEngine.generateResponse(prompt)
                                                                                }

                                                                                        // 4. 若無備援引擎且主引擎失敗，則返回主引擎的錯誤
                                                                                                return@withContext primaryResult
                                }
   }
   
                                                                                }
                                                                }}
   }
   )