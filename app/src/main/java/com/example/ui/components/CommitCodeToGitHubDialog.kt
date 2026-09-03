package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.GitHubDeviceCodeResponse
import com.example.data.api.GitHubRepoResponse
import com.example.data.api.GitHubUserProfile
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.utils.IntentHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommitCodeToGitHubDialog(
    initialCode: String,
    language: String,
    isAuthenticated: Boolean,
    userProfile: GitHubUserProfile?,
    userRepositories: List<GitHubRepoResponse>,
    defaultRepo: String,
    deviceCodeResponse: GitHubDeviceCodeResponse?,
    isOAuthPolling: Boolean,
    onStartDeviceOAuth: () -> Unit,
    onCancelDeviceOAuth: () -> Unit,
    onRefreshProfile: () -> Unit,
    onCommitCode: (repo: String, filePath: String, code: String, commitMsg: String, branch: String, onResult: (Boolean, String, String?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Determine smart default filename based on language
    val defaultFilename = remember(language) {
        when (language.lowercase().trim()) {
            "kotlin", "kt" -> "app/src/main/java/com/example/AgentGenerated.kt"
            "java" -> "src/main/java/com/example/AgentGenerated.java"
            "python", "py" -> "src/agent_script.py"
            "javascript", "js" -> "src/index.js"
            "typescript", "ts" -> "src/index.ts"
            "json" -> "data/config.json"
            "yaml", "yml" -> ".github/workflows/agent-pipeline.yml"
            "bash", "sh", "shell" -> "scripts/deploy.sh"
            "html" -> "public/index.html"
            "markdown", "md" -> "DOCS.md"
            "sql" -> "db/schema.sql"
            else -> "src/generated_code.txt"
        }
    }

    var targetRepo by remember {
        mutableStateOf(
            if (defaultRepo.isNotBlank()) defaultRepo
            else if (userProfile != null && userRepositories.isNotEmpty()) userRepositories.first().fullName
            else if (userProfile != null) "${userProfile.login}/nexus-generated-code"
            else "your-username/your-repo"
        )
    }

    var filePath by remember { mutableStateOf(defaultFilename) }
    var branch by remember { mutableStateOf("main") }
    var commitMessage by remember {
        mutableStateOf(
            "feat(agent): commit generated $language code by Nexus AI Agent"
        )
    }
    var codeContent by remember { mutableStateOf(initialCode) }

    var isSubmitting by remember { mutableStateOf(false) }
    var commitSuccessUrl by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }
    var repoDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .testTag("commit_code_to_github_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "提交代码至 GitHub 仓库",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "将 Agent 生成的代码直接 Commit & Push 到个人公开仓库",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OAuth Status & Authentication Card
                if (!isAuthenticated || userProfile == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = ElectricPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "需要 GitHub 账号授权",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE9D5FF)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "请先完成 GitHub OAuth 授权，以便 Nexus 能够直接向您的个人公开仓库提交代码：",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // OAuth Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onStartDeviceOAuth,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_start_device_oauth"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("设备码授权", fontSize = 12.sp)
                                }
                            }

                            // Device Code Flow Active Display
                            if (deviceCodeResponse != null && isOAuthPolling) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "设备授权验证码",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = deviceCodeResponse.userCode,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = CyanPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = CyanPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "请在浏览器打开 github.com/login/device 确认授权",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    IntentHelper.copyToClipboard(context, "GitHub 授权码", deviceCodeResponse.userCode)
                                                    IntentHelper.openBrowserUrl(context, deviceCodeResponse.verificationUri)
                                                },
                                                modifier = Modifier.height(32.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("复制码并打开网页", fontSize = 11.sp, color = Slate900)
                                            }

                                            OutlinedButton(
                                                onClick = onCancelDeviceOAuth,
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("取消", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // Logged in User Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "@${userProfile.login}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "已授权",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${userProfile.publicRepos} 个公开仓库",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRefreshProfile,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新仓库列表",
                                    tint = Cyan80,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Target Repository Selector
                Text(
                    text = "目标公开仓库 (Target Repository)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cyan80
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (userRepositories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = repoDropdownExpanded,
                        onExpandedChange = { repoDropdownExpanded = !repoDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = targetRepo,
                            onValueChange = { targetRepo = it },
                            placeholder = { Text("例如：username/my-project", fontSize = 13.sp, color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repoDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("target_repo_input")
                        )

                        ExposedDropdownMenu(
                            expanded = repoDropdownExpanded,
                            onDismissRequest = { repoDropdownExpanded = false },
                            modifier = Modifier.background(Slate800)
                        ) {
                            userRepositories.forEach { repoItem ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = repoItem.fullName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (!repoItem.description.isNullOrBlank()) {
                                                Text(
                                                    text = repoItem.description,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        targetRepo = repoItem.fullName
                                        if (repoItem.defaultBranch.isNotBlank()) {
                                            branch = repoItem.defaultBranch
                                        }
                                        repoDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = targetRepo,
                        onValueChange = { targetRepo = it },
                        placeholder = { Text("例如：username/my-project", fontSize = 13.sp, color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_repo_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File Path & Branch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(2.5f)) {
                        Text(
                            text = "文件相对路径",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan80
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = filePath,
                            onValueChange = { filePath = it },
                            placeholder = { Text("src/Main.kt", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_path_input")
                        )
                    }

                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "分支 (Branch)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan80
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = branch,
                            onValueChange = { branch = it },
                            placeholder = { Text("main", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("target_branch_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Commit Message
                Text(
                    text = "提交说明 (Commit Message)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cyan80
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    placeholder = { Text("feat: add agent generated code", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commit_message_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Code Preview Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "代码内容预览 ($language - ${codeContent.lines().size} 行)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${codeContent.length} 字符",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0B1120),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = codeContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Status message display
                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isStatusError) CoralRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isStatusError) CoralRed.copy(alpha = 0.5f) else EmeraldGreen.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isStatusError) Icons.Default.Close else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isStatusError) CoralRed else EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = statusMessage ?: "",
                                    fontSize = 12.sp,
                                    color = if (isStatusError) CoralRed else EmeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (!commitSuccessUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        IntentHelper.openBrowserUrl(context, commitSuccessUrl!!)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Slate900
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("在 GitHub 中查看文件", fontSize = 11.sp, color = Slate900, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("取消", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (targetRepo.isBlank() || filePath.isBlank()) {
                                statusMessage = "请填写目标仓库与文件路径"
                                isStatusError = true
                                return@Button
                            }
                            isSubmitting = true
                            statusMessage = "正在向 GitHub 提交代码..."
                            isStatusError = false

                            onCommitCode(
                                targetRepo,
                                filePath,
                                codeContent,
                                commitMessage,
                                branch
                            ) { success, msg, url ->
                                isSubmitting = false
                                isStatusError = !success
                                statusMessage = msg
                                commitSuccessUrl = url
                            }
                        },
                        enabled = !isSubmitting && isAuthenticated,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("submit_git_commit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Slate900,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在推送...", fontSize = 13.sp, color = Slate900)
                        } else {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Slate900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "立即 Commit & Push",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                    }
                }
            }
        }
    }
}
