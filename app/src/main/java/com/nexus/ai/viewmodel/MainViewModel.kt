package com.nexus.ai.viewmodel // 宣告此檔案隸屬的套件路徑

import androidx.lifecycle.ViewModel // 引用 Android 官方 ViewModel 基底類別
import androidx.lifecycle.viewModelScope // 引用 ViewModel 協程範疇 (生命週期結束時自動釋放)
import com.nexus.ai.engine.HybridAiRepository // 引用先前建立的雙引擎儲存庫
import kotlinx.coroutines.flow.MutableStateFlow // 引用可變動的 StateFlow
import kotlinx.coroutines.flow.StateFlow // 引用唯讀的 StateFlow
import kotlinx.coroutines.flow.asStateFlow // 引用狀態轉換工具
import kotlinx.coroutines.launch // 引用啟動協程的方法

/**
 * 畫面狀態密封介面：定義 Compose 介面可能遇到的每一種狀態
  */
  sealed interface AiUiState {
        object Idle : AiUiState // 閒置狀態 (等待使用者輸入)
            object Loading : AiUiState // 生成中 (顯示加載動畫)
                data class Success(val result: String) : AiUiState // 成功取得 AI 回傳文字
                    data class Error(val message: String) : AiUiState // 請求失敗顯示錯誤訊息
  }

  /**
   * MainViewModel：連接 UI 介面與雙引擎 Repository 的橋樑
    */
    class MainViewModel(
            private val repository: HybridAiRepository // 注入混合 AI 儲存庫
    ) : ViewModel() {

            // 內部私有狀態，預設為 Idle
                private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
                    
                        // 對外暴露的唯讀 StateFlow，供 Compose 畫面監聽與刷新
                            val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

                                /**
                                     * 發送 Prompt 給 AI 引擎
                                          * @param prompt 使用者在文字框輸入的提示詞
                                               */
                                                   fun sendPrompt(prompt: String) {
                                                            if (prompt.isBlank()) return // 輸入空白文字時直接忽略

                                                                    // 啟動背景協程處理請求
                                                                            viewModelScope.launch {
                                                                                            _uiState.value = AiUiState.Loading // 1. 將畫面切換至 Loading 狀態
                                                                                                        
                                                                                                                    // 2. 調用 Repository (內部會自動處理 Gemini -> OpenRouter 自動切換)
                                                                                                                                val result = repository.getCompletion(prompt)

                                                                                                                                            // 3. 根據回傳結果更新 State
                                                                                                                                                        result.fold(
                                                                                                                                                                            onSuccess = { responseText ->
                                                                                                                                                                                                _uiState.value = AiUiState.Success(responseText) // 成功：傳遞 AI 回答
                                                                                                                                                                                                                },
                                                                                                                                                                                                                                onFailure = { throwable ->
                                                                                                                                                                                                                                                    _uiState.value = AiUiState.Error(throwable.localizedMessage ?: "未知錯誤") // 失敗：傳遞錯誤訊息
                                                                                                                                                                                                                                                                    }
                                                                                                                                                        )
                                                                            }
                                                   }
    }
    
                                                                                                                                                        )
                                                                            }
                                                   }
    }
    )
  }