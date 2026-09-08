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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.nexus.NexusFeature

private enum class NexusTab(val label: String) {
    HOME("首页"),
    CHAT("对话"),
    MORE("更多")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(viewModel: ChatViewModel) {
    var tab by remember { mutableStateOf(NexusTab.HOME) }
    var selectedFeature by remember { mutableStateOf<NexusFeature?>(null) }

    val feature = selectedFeature
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(feature?.title ?: "Nexus 智能助手") },
                navigationIcon = {
                    if (feature != null) {
                        androidx.compose.material3.TextButton(onClick = { selectedFeature = null }) {
                            Text("返回")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NexusTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item && feature == null,
                        onClick = {
                            selectedFeature = null
                            tab = item
                        },
                        icon = { Text(item.label.take(1)) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                feature != null -> NexusFeatureScreen(feature)
                tab == NexusTab.HOME -> NexusHomeScreen(
                    onOpenChat = { tab = NexusTab.CHAT },
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
