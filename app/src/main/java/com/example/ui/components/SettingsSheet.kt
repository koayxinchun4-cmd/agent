package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import com.example.data.api.GitHubUserProfile
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentApiKey: String,
    selectedModel: String,
    githubUserProfile: GitHubUserProfile? = null,
    githubRepo: String = "google/mesop",
    onSaveApiKey: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onExportZip: () -> Unit,
    onOpenGitHubStudio: () -> Unit = {},
    onStartGitHubOAuth: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "智能体设置与模型引擎",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "配置 Google Gemini API、GitHub OAuth 与 CI/CD",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "免 Root 运行就绪",
                            fontSize = 11.sp,
                            color = Color(0xFF6EE7B7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // GitHub OAuth & CI/CD Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = BorderStroke(1.dp, if (githubUserProfile != null) EmeraldGreen.copy(alpha = 0.4f) else Color(0xFF222F46))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GitHub OAuth 与 CI/CD 工作流", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }

                        if (githubUserProfile != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, EmeraldGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("@${githubUserProfile.login}", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = if (githubUserProfile != null)
                            "已成功绑定 GitHub 账号。Agent 具备自动化 Commit/Push、创建 PR 以及向目标仓库注入 CI/CD 工作流的权限。"
                        else
                            "通过 GitHub OAuth 官方协议授权后，智能体将具备向仓库执行 Commit、推送代码及自动部署 GitHub Actions CI/CD 流水线的能力。",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )

                    Surface(
                        color = Color(0xFF0B101B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("当前目标仓库", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(githubRepo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary, fontFamily = FontFamily.Monospace)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = if (githubUserProfile != null) "OAuth Token 活跃" else "未认证",
                                    fontSize = 10.sp,
                                    color = if (githubUserProfile != null) Color(0xFF6EE7B7) else Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (githubUserProfile == null) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onStartGitHubOAuth()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.weight(1f).testTag("settings_start_oauth_btn")
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("开启 GitHub OAuth", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onOpenGitHubStudio()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (githubUserProfile != null) CyanPrimary else Color(0xFF1E293B)),
                            border = if (githubUserProfile == null) BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.weight(1f).testTag("settings_open_cicd_studio_btn")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = if (githubUserProfile != null) Color.Black else Cyan80, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CI/CD & 智能体工坊", color = if (githubUserProfile != null) Color.Black else Cyan80, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gemini API Key Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = BorderStroke(1.dp, Color(0xFF222F46))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini API Key (免费额度)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    Text(
                        text = "💡 选填项目：本应用内置免配置的「CTO.new 自主全栈软件工程师引擎」，即使不填写 API Key 亦可完全自主运行。若需接入 Google 官方实时 Gemini 大模型，可在此填入 AI Studio 免费 Key。",
                        fontSize = 12.sp,
                        color = Color(0xFF6EE7B7),
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("输入 AI Studio API Key (如 AIza...)") },
                        placeholder = { Text("在此粘贴您的 Gemini API Key") },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "切换可见性",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = { onSaveApiKey(apiKeyInput.trim()) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("save_api_key_button")
                    ) {
                        Text("保存并应用 API Key", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Model Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = BorderStroke(1.dp, Color(0xFF222F46))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini 基础模型选择", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    val models = listOf(
                        "gemini-2.5-flash" to "极速响应、低延迟，适合移动端与日常交互",
                        "gemini-2.5-pro" to "深度逻辑推理、擅长复杂代码与长文档分析"
                    )

                    models.forEach { (modelKey, desc) ->
                        val isSelected = selectedModel == modelKey
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("model_option_$modelKey"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF1E2A42) else Color(0xFF0F1522),
                            border = BorderStroke(1.dp, if (isSelected) CyanPrimary else Color(0xFF233148)),
                            onClick = { onSelectModel(modelKey) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = modelKey,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) CyanPrimary else Color.White
                                    )
                                    Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Export Chat Transcripts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111726)),
                border = BorderStroke(1.dp, Color(0xFF222F46))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = Cyan80, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出对话为 ZIP 压缩包", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    Text(
                        text = "将当前会话打包为标准 zip 压缩包，内含 chat_transcript.md、chat_transcript.txt 以及元数据 metadata.json。",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Button(
                        onClick = onExportZip,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("export_zip_button")
                    ) {
                        Text("生成并分享 ZIP 导出文件", color = Cyan80, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
