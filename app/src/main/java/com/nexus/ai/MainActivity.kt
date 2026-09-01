package com.nexus.ai // 宣告此檔案隸屬的套件路徑

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.ai.engine.GeminiEngine
import com.nexus.ai.engine.HybridAiRepository
import com.nexus.ai.engine.OpenRouterEngine
import com.nexus.ai.viewmodel.AiUiState
import com.nexus.ai.viewmodel.MainViewModel

/**
 * Android 主入口 Activity：負責初始化 UI 介面與雙引擎 ViewModel
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化雙 AI 引擎 (預設可填入你的 Key 或先傳空字串測試)
        val geminiApiKey = "" 
        val openRouterApiKey = ""

        val geminiEngine = GeminiEngine(apiKey = geminiApiKey)
        val openRouterEngine = OpenRouterEngine(apiKey = openRouterApiKey)

        // 2. 初始化混合儲存庫 (Gemini 為主引擎，OpenRouter 為備援引擎)
        val repository = HybridAiRepository(
            primaryEngine = geminiEngine,
            fallbackEngine = openRouterEngine
        )

        // 3. 初始化控制邏輯的 ViewModel
        val viewModel = MainViewModel(repository)

        // 4. 設定 Jetpack Compose UI 內容
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexusAiScreen(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * 主畫面 Compose 元件：負責渲染輸入框、按鈕與 AI 回應結果
 */
@Composable
fun NexusAiScreen(viewModel: MainViewModel) {
    // 監聽 ViewModel 的 UI 狀態變更 (自動觸發畫面重繪)
    val uiState by viewModel.uiState.collectAsState()

    // 記錄輸入框內的文字狀態
    var promptText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 頂部標題列
        Text(
            text = "Nexus AI Agent",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 中間顯示 AI 回應結果的滾動區域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            when (val state = uiState) {
                is AiUiState.Idle -> {
                    Text(
                        text = "請在下方輸入 Prompt 開始對話...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AiUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AiUiState.Success -> {
                    Text(
                        text = state.result,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is AiUiState.Error -> {
                    Text(
                        text = "錯誤: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 底部輸入區域 (文字輸入框 + 發送按鈕)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("輸入提示詞") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.sendPrompt(promptText)
                    promptText = "" // 發送後清空輸入框
                },
                enabled = uiState !is AiUiState.Loading // 載入中時停用按鈕
            ) {
                Text("發送")
            }
        }
    }
}
