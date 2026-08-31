package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.GitHubDeviceCodeResponse
import com.example.data.api.GitHubIssue
import com.example.data.api.GitHubRepoResponse
import com.example.data.api.GitHubUserProfile
import com.example.data.api.OfficeAgent
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OfficeAgentStudioSheet(
    officeAgents: List<OfficeAgent>,
    currentRepo: String,
    repoDetails: GitHubRepoResponse?,
    issues: List<GitHubIssue>,
    userProfile: GitHubUserProfile?,
    userRepositories: List<GitHubRepoResponse> = emptyList(),
    isGitHubLoading: Boolean,
    statusMessage: String?,
    githubToken: String,
    deviceCodeData: GitHubDeviceCodeResponse?,
    isOAuthPolling: Boolean,
    onSaveRepo: (String) -> Unit,
    onSaveToken: (String) -> Unit,
    onUnbindAccount: () -> Unit,
    onStartWebOAuth: (() -> Unit)? = null,
    onStartDeviceOAuth: () -> Unit,
    onCancelDeviceOAuth: () -> Unit,
    onRefreshUserProfile: () -> Unit,
    onRefreshRepo: (String) -> Unit,
    onCreateIssue: (String, String, List<String>) -> Unit,
    onCommitAndPush: (String, String, String, String?, (Boolean, String) -> Unit) -> Unit,
    onCreatePr: (String, String, String, String?, (Boolean, String) -> Unit) -> Unit,
    onDispatchAgent: (OfficeAgent, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 智能体矩阵, 1: 自动化 Git 提交, 2: 仓库 Issues, 3: GitHub 账号与 OAuth

    var repoInput by remember { mutableStateOf(currentRepo) }
    var tokenInput by remember { mutableStateOf(githubToken) }
    var showCreateIssueDialog by remember { mutableStateOf(false) }
    var newIssueTitle by remember { mutableStateOf("") }
    var newIssueBody by remember { mutableStateOf("") }

    // Commit & Push states
    var commitFilePath by remember { mutableStateOf("app/README.md") }
    var commitMessage by remember { mutableStateOf("docs(agent): update project documentation by Nexus Agent") }
    var commitBranch by remember { mutableStateOf(repoDetails?.defaultBranch ?: "main") }
    var commitContent by remember {
        mutableStateOf(
            "# Nexus Automated Workspace\n\nAuto-generated and committed by Nexus Android AI Agent.\n- Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n- Status: Verified"
        )
    }
    var lastCommitActionResult by remember { mutableStateOf<String?>(null) }

    // PR states
    var prTitle by remember { mutableStateOf("feat(agent): automated patch by Nexus Agent") }
    var prHeadBranch by remember { mutableStateOf("patch-1") }
    var prBaseBranch by remember { mutableStateOf(repoDetails?.defaultBranch ?: "main") }
    var prBody by remember { mutableStateOf("This pull request was automatically generated by Nexus Agent.") }

    var selectedAgentForCustomTask by remember { mutableStateOf<OfficeAgent?>(null) }
    var customTaskPrompt by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
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
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "办公室智能体工坊 • GitHub 作战室",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (userProfile != null) "已认证: @${userProfile.login} (支持自动化 Commit / Push)" else "未认证: 请绑定 GitHub OAuth 以授权自动提交",
                            fontSize = 11.sp,
                            color = if (userProfile != null) Color(0xFF6EE7B7) else Color(0xFFFBBF24)
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GitHub Repo Quick Bar
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = repoInput,
                            onValueChange = { repoInput = it },
                            placeholder = { Text("owner/repo 例如 google/mesop", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("github_repo_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0B0F19),
                                unfocusedContainerColor = Color(0xFF0B0F19)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                onSaveRepo(repoInput)
                                onRefreshRepo(repoInput)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .testTag("github_refresh_button")
                        ) {
                            if (isGitHubLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (repoDetails != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stars Badge
                            Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(6.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${repoDetails.stargazersCount} Stars", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Open Issues Badge
                            Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(6.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = CoralRed, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${repoDetails.openIssuesCount} Issues", color = CoralRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Branch Badge
                            Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "分支: ${repoDetails.defaultBranch}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs (4 Tabs)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CyanPrimary,
                edgePadding = 0.dp,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("智能体矩阵 (${officeAgents.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⚡ 自动化 Commit & Push", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("仓库 Issues (${issues.size})", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔑 OAuth & 账号", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            if (userProfile != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab 0: Office Agents Matrix
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(officeAgents) { agent ->
                        OfficeAgentCard(
                            agent = agent,
                            currentRepo = repoInput,
                            onDispatch = { prompt ->
                                onDispatchAgent(agent, prompt)
                                onDismiss()
                            },
                            onOpenCustomDispatch = {
                                selectedAgentForCustomTask = agent
                                customTaskPrompt = agent.defaultPromptTemplate.replace("%REPO%", repoInput)
                            }
                        )
                    }
                }
            }

            // Tab 1: Automated Git Commit & Push Studio
            if (selectedTab == 1) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Commit, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Agent 自动化 Git Commit & Push 终端", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Surface(
                                        color = if (githubToken.isNotBlank()) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (githubToken.isNotBlank()) "已就绪" else "需要绑定账号",
                                            color = if (githubToken.isNotBlank()) Color(0xFF6EE7B7) else Color(0xFFFBBF24),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "允许 Nexus 智能体调用已授权的 GitHub OAuth 凭证直接将文件变更、修复补丁或 CI 工作流自动 Commit 并 Push 至目标分支。",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 15.sp
                                )

                                // Fast template pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            commitFilePath = ".github/workflows/ci.yml"
                                            commitMessage = "ci: add automated CI build workflow by Nexus Agent"
                                            commitContent = """
                                                name: Android CI & Build

                                                on:
                                                  push:
                                                    branches: [ main, master ]
                                                  pull_request:
                                                    branches: [ main, master ]

                                                jobs:
                                                  build:
                                                    runs-on: ubuntu-latest
                                                    steps:
                                                    - uses: actions/checkout@v4
                                                    - name: Set up JDK 17
                                                      uses: actions/setup-java@v4
                                                      with:
                                                        java-version: '17'
                                                        distribution: 'temurin'
                                                    - name: Grant execute permission for gradlew
                                                      run: chmod +x gradlew || true
                                                    - name: Build with Gradle
                                                      run: ./gradlew assembleDebug || gradle assembleDebug
                                            """.trimIndent()
                                        },
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, CyanPrimary)
                                    ) {
                                        Text("CI 工作流模板", fontSize = 10.sp, color = CyanPrimary)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            commitFilePath = "README.md"
                                            commitMessage = "docs(readme): update project features and setup guide"
                                            commitContent = """
                                                # ${repoInput.substringAfterLast("/")}

                                                Powered by Nexus Android Multi-Agent AI System.

                                                ## 🚀 Features
                                                - Native Android Speech-to-Text Integration
                                                - Multi-Agent GitHub Copilot & CI/CD Automation
                                                - Type-Safe Jetpack Compose UI
                                                - Offline Long-Term Memory
                                            """.trimIndent()
                                        },
                                        modifier = Modifier.weight(1f).height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, Color(0xFF38BDF8))
                                    ) {
                                        Text("README 模板", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                    }
                                }

                                // Target Branch & File Path
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = commitBranch,
                                        onValueChange = { commitBranch = it },
                                        label = { Text("目标分支", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanPrimary,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    OutlinedTextField(
                                        value = commitFilePath,
                                        onValueChange = { commitFilePath = it },
                                        label = { Text("文件相对路径", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.4f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanPrimary,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }

                                OutlinedTextField(
                                    value = commitMessage,
                                    onValueChange = { commitMessage = it },
                                    label = { Text("Git Commit 信息 (Conventional Commits)", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = commitContent,
                                    onValueChange = { commitContent = it },
                                    label = { Text("文件内容 / 代码补丁", fontSize = 11.sp) },
                                    minLines = 4,
                                    maxLines = 8,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF090D16),
                                        unfocusedContainerColor = Color(0xFF090D16)
                                    )
                                )

                                if (lastCommitActionResult != null) {
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = lastCommitActionResult!!,
                                            fontSize = 11.sp,
                                            color = if (lastCommitActionResult!!.contains("成功")) Color(0xFF6EE7B7) else CoralRed,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        lastCommitActionResult = "正在向 GitHub 执行 Commit & Push..."
                                        onCommitAndPush(commitFilePath, commitContent, commitMessage, commitBranch) { success, msg ->
                                            lastCommitActionResult = msg
                                        }
                                    },
                                    enabled = commitFilePath.isNotBlank() && commitMessage.isNotBlank() && !isGitHubLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("execute_commit_push_button")
                                ) {
                                    if (isGitHubLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text("🚀 授权 Agent 执行 Commit & Push", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // PR Creation Section
                    item {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("一键创建 GitHub Pull Request", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = prHeadBranch,
                                        onValueChange = { prHeadBranch = it },
                                        label = { Text("来源分支 (Head)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElectricPurple,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    OutlinedTextField(
                                        value = prBaseBranch,
                                        onValueChange = { prBaseBranch = it },
                                        label = { Text("目标分支 (Base)", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElectricPurple,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }

                                OutlinedTextField(
                                    value = prTitle,
                                    onValueChange = { prTitle = it },
                                    label = { Text("PR 标题", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricPurple,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Button(
                                    onClick = {
                                        onCreatePr(prTitle, prHeadBranch, prBaseBranch, prBody) { success, msg ->
                                            lastCommitActionResult = msg
                                        }
                                    },
                                    enabled = prTitle.isNotBlank() && !isGitHubLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔀 创建 Pull Request", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Repo Issues View
            if (selectedTab == 2) {
                if (issues.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("当前仓库暂无开放的 Issues，或尚未连接仓库", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onRefreshRepo(repoInput) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                            ) {
                                Text("立即同步 GitHub Issues", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(issues) { issue ->
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${issue.number} ${issue.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(color = CoralRed.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                            Text("OPEN", color = CoralRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }

                                    if (!issue.body.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = issue.body,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                val triageAgent = officeAgents.find { it.id == "agent_issue_triage" } ?: officeAgents.first()
                                                val prompt = "请分析 GitHub Issue #${issue.number}【${issue.title}】并提供复现用例与完整修复代码：\n\nIssue 内容：\n${issue.body ?: "无描述"}"
                                                onDispatchAgent(triageAgent, prompt)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("指派 Issue 巡检官修复", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 3: GitHub OAuth Authentication & Account Binding
            if (selectedTab == 3) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Card if logged in
                    if (userProfile != null) {
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!userProfile.avatarUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = userProfile.avatarUrl,
                                                    contentDescription = "GitHub Avatar",
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, Color(0xFF10B981), CircleShape)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(CyanPrimary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🐙", fontSize = 20.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = userProfile.name ?: userProfile.login,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.CheckCircle, contentDescription = "已认证", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                                }
                                                Text(
                                                    text = "@${userProfile.login}",
                                                    fontSize = 12.sp,
                                                    color = Cyan80
                                                )
                                            }
                                        }

                                        IconButton(onClick = onRefreshUserProfile, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Refresh, contentDescription = "刷新用户信息", tint = CyanPrimary)
                                        }
                                    }

                                    if (!userProfile.bio.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = userProfile.bio, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(color = Slate800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("公开仓库", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                Text("${userProfile.publicRepos}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                        Surface(color = Slate800, shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("关注者", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                Text("${userProfile.followers}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = onUnbindAccount,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("解除当前 GitHub 账号绑定", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // OAuth Device Authorization Flow
                    item {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GitHub OAuth 官方安全授权 (Device Flow)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Text(
                                    text = "采用 GitHub 官方 RFC 8628 Device Authorization 协议。在应用内一键获取授权码并在浏览器批准后，智能体将自动获得 Repo 写入与提交权限。",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 15.sp
                                )

                                if (deviceCodeData != null && isOAuthPolling) {
                                    // Device Flow In-Progress Banner
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, CyanPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("你的 GitHub 授权码为：", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = deviceCodeData.userCode,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = CyanPrimary,
                                                    letterSpacing = 2.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(deviceCodeData.userCode))
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "复制授权码", tint = Cyan80, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deviceCodeData.verificationUri))
                                                        context.startActivity(intent)
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                                    modifier = Modifier.weight(1.4f).height(38.dp)
                                                ) {
                                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("打开 GitHub 授权页面", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = onCancelDeviceOAuth,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF475569)),
                                                    modifier = Modifier.weight(0.8f).height(38.dp)
                                                ) {
                                                    Text("取消", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CyanPrimary, strokeWidth = 1.5.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("正在安全等待 GitHub 页面批准授权...", fontSize = 10.sp, color = Cyan80)
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (onStartWebOAuth != null) {
                                            Button(
                                                onClick = onStartWebOAuth,
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .testTag("start_web_oauth_button")
                                            ) {
                                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("网页一键授权", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = onStartDeviceOAuth,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("start_oauth_device_flow_button")
                                        ) {
                                            Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("设备码授权", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Direct Personal Access Token (PAT) Option
                    item {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("或者手动配置 Personal Access Token (PAT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("支持直接粘贴具备 repo、workflow 权限的 GitHub PAT (如 ghp_xxx)。", fontSize = 11.sp, color = Color(0xFF94A3B8))

                                OutlinedTextField(
                                    value = tokenInput,
                                    onValueChange = { tokenInput = it },
                                    placeholder = { Text("ghp_xxxxxxxxxxxx", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("github_pat_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Button(
                                    onClick = { onSaveToken(tokenInput) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("保存并验证 Token", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Create Issue Section
                    item {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("在 GitHub 仓库中创建新 Issue", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                OutlinedTextField(
                                    value = newIssueTitle,
                                    onValueChange = { newIssueTitle = it },
                                    label = { Text("Issue 标题 (例如: [Bug] 内存泄漏排查)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricPurple,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = newIssueBody,
                                    onValueChange = { newIssueBody = it },
                                    label = { Text("Issue 详细描述 / 复现步骤") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricPurple,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (newIssueTitle.isNotBlank()) {
                                            onCreateIssue(newIssueTitle, newIssueBody, listOf("nexus-agent", "automated"))
                                            newIssueTitle = ""
                                            newIssueBody = ""
                                        }
                                    },
                                    enabled = newIssueTitle.isNotBlank() && !isGitHubLoading,
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("一键提交至 GitHub", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Custom Dispatch Dialog / Inline Box
            if (selectedAgentForCustomTask != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "指派任务给：${selectedAgentForCustomTask?.avatarEmoji} ${selectedAgentForCustomTask?.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            IconButton(onClick = { selectedAgentForCustomTask = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "取消", tint = Color.Gray)
                            }
                        }

                        OutlinedTextField(
                            value = customTaskPrompt,
                            onValueChange = { customTaskPrompt = it },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                selectedAgentForCustomTask?.let { agent ->
                                    onDispatchAgent(agent, customTaskPrompt)
                                    selectedAgentForCustomTask = null
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("立即启动智能体执行", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfficeAgentCard(
    agent: OfficeAgent,
    currentRepo: String,
    onDispatch: (String) -> Unit,
    onOpenCustomDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = agent.avatarEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = agent.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("就绪", color = Color(0xFF6EE7B7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(text = agent.roleTitle, fontSize = 11.sp, color = Cyan80)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = agent.description, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Capabilities Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                agent.capabilities.forEach { cap ->
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = cap,
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenCustomDispatch,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Text("自定义指令", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }

                Button(
                    onClick = {
                        val prompt = agent.defaultPromptTemplate.replace("%REPO%", currentRepo)
                        onDispatch(prompt)
                    },
                    modifier = Modifier.weight(1.2f).height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("派发 GitHub 任务", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
