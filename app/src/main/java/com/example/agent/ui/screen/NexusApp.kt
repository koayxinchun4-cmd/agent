package com.example.agent.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
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

private const val LANGUAGE_PREFS = "nexus_preferences"
private const val LANGUAGE_KEY = "language"

private fun loadLanguage(context: Context): NexusLanguage {
    val code = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        .getString(LANGUAGE_KEY, NexusLanguage.ENGLISH.code)
    return NexusLanguage.entries.firstOrNull { it.code == code } ?: NexusLanguage.ENGLISH
}

private fun saveLanguage(context: Context, language: NexusLanguage) {
    context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(LANGUAGE_KEY, language.code)
        .apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(
    viewModel: ChatViewModel,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(NexusTab.HOME) }
    var selectedFeature by remember { mutableStateOf<NexusFeature?>(null) }
    var showTask by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(loadLanguage(context)) }

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
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
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
                    onLanguageSelected = {
                        language = it
                        saveLanguage(context, it)
                    }
                )
                showTask -> TaskScreen(
                    onBack = { showTask = false },
                    viewModel = taskViewModel
                )
                feature != null -> NexusFeatureScreen(feature, language)
                tab == NexusTab.HOME -> NexusHomeScreen(
                    onOpenChat = { tab = NexusTab.CHAT },
                    onStartTask = { showTask = true },
                    onOpenFeature = { selectedFeature = it }
                )
                tab == NexusTab.CHAT -> ChatScreen(viewModel)
                else -> NexusMoreScreen(language)
            }
        }
    }
}

@Composable
private fun NexusFeatureScreen(feature: NexusFeature, language: NexusLanguage) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("${feature.symbol}  ${feature.title}", style = MaterialTheme.typography.headlineSmall)
        Text(feature.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        Text(
            when (language) {
                NexusLanguage.ENGLISH -> "This capability is part of the Nexus architecture. Android-native integrations will be added progressively while keeping the phone-only, no-root design."
                NexusLanguage.CHINESE -> "此能力已纳入 Nexus 架构。后续会逐步接入 Android 原生能力，并保持手机端、免 Root 的设计。"
                NexusLanguage.MALAY -> "Keupayaan ini ialah sebahagian daripada seni bina Nexus. Integrasi Android asli akan ditambah secara berperingkat sambil mengekalkan reka bentuk telefon sahaja tanpa root."
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun NexusMoreScreen(language: NexusLanguage) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Nexus", style = MaterialTheme.typography.headlineSmall)
        Text(
            when (language) {
                NexusLanguage.ENGLISH -> "Native mobile AI Agent"
                NexusLanguage.CHINESE -> "纯手机 AI Agent"
                NexusLanguage.MALAY -> "AI Agent mudah alih asli"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            when (language) {
                NexusLanguage.ENGLISH -> "Capabilities are added as modular features: Skills, Memory, AI Moments, Web Research, File Agent, App Agent, Office, GitHub / Codex, Voice Assistant, and AI Community."
                NexusLanguage.CHINESE -> "功能会以模块化方式逐步加入：Skills、Memory、AI Moments、Web Research、文件与 App Agent、Office、GitHub/Codex、语音和 AI 朋友圈。"
                NexusLanguage.MALAY -> "Keupayaan ditambah secara modular: Skills, Memory, AI Moments, Web Research, File Agent, App Agent, Office, GitHub / Codex, Voice Assistant dan AI Community."
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
