package com.example.agent.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.nexus.NexusFeature

private enum class NexusTab(val labelZh: String, val labelEn: String, val labelMs: String) {
    HOME("首页", "Home", "Utama"),
    CHAT("对话", "Chat", "Sembang"),
    MORE("更多", "More", "Lagi")
}

private fun NexusTab.label(language: NexusLanguage): String = when (language) {
    NexusLanguage.CHINESE -> labelZh
    NexusLanguage.ENGLISH -> labelEn
    NexusLanguage.MALAY -> labelMs
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(
    viewModel: ChatViewModel,
    taskViewModel: TaskViewModel
) {
    var tab by remember { mutableStateOf(NexusTab.HOME) }
    var selectedFeature by remember { mutableStateOf<NexusFeature?>(null) }
    var showTask by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(NexusLanguage.ENGLISH) }

    val feature = selectedFeature
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            showSettings -> when (language) {
                                NexusLanguage.CHINESE -> "设置"
                                NexusLanguage.ENGLISH -> "Settings"
                                NexusLanguage.MALAY -> "Tetapan"
                            }
                            showTask -> "Nexus Task"
                            feature != null -> feature.title
                            else -> "Nexus"
                        }
                    )
                },
                navigationIcon = {
                    if (showSettings || showTask || feature != null) {
                        TextButton(onClick = {
                            showSettings = false
                            showTask = false
                            selectedFeature = null
                        }) {
                            Text(
                                when (language) {
                                    NexusLanguage.CHINESE -> "返回"
                                    NexusLanguage.ENGLISH -> "Back"
                                    NexusLanguage.MALAY -> "Kembali"
                                }
                            )
                        }
                    }
                },
                actions = {
                    if (!showSettings && !showTask && feature == null) {
                        TextButton(onClick = { showSettings = true }) {
                            Text("⚙")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!showSettings) {
                NavigationBar {
                    NexusTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item && feature == null && !showTask,
                            onClick = {
                                showTask = false
                                selectedFeature = null
                                tab = item
                            },
                            icon = { Text(item.label(language).take(1)) },
                            label = { Text(item.label(language)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                showSettings -> NexusSettingsScreen(
                    language = language,
                    onLanguageSelected = { language = it }
                )
                showTask -> TaskScreen(
                    onBack = { showTask = false },
                    viewModel = taskViewModel
                )
                feature != null -> NexusFeatureScreen(feature)
                tab == NexusTab.HOME -> NexusHomeScreen(
                    onOpenChat = { tab = NexusTab.CHAT },
                    onStartTask = { showTask = true },
                    onOpenFeature = { selectedFeature = it }
                )
                tab == NexusTab.CHAT -> ChatScreen(viewModel)
                else -> NexusMoreScreen()
            }
        }
    }
}

@Composable
private fun NexusFeatureScreen(feature: NexusFeature) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("${feature.symbol}  ${feature.title}", style = MaterialTheme.typography.headlineSmall)
        Text(feature.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        Text(
            "模块已纳入 Nexus 架构。下一阶段会接入 Android 原生能力，并保持手机端、免 Root 的设计。",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun NexusMoreScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Nexus", style = MaterialTheme.typography.headlineSmall)
        Text("纯手机 AI Agent", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Text(
            "功能会以模块化方式逐步加入：Skills、Memory、AI Moments、Web Research、文件与 App Agent、Office、GitHub/Codex、语音和 AI 朋友圈。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
