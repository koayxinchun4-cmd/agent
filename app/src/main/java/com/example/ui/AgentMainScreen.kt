package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ActiveSkillBanner
import com.example.ui.components.AiMomentsHubSheet
import com.example.ui.components.ChatBubble
import com.example.ui.components.CommitCodeToGitHubDialog
import com.example.ui.components.MemorySheet
import com.example.ui.components.OfficeAgentStudioSheet
import com.example.ui.components.OfficeStatusBar
import com.example.ui.components.SessionsDrawer
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SkillsSheet
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.utils.IntentHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentMainScreen(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val activeSkillId by viewModel.activeSkillId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val recognizedSpeechText by viewModel.recognizedSpeechText.collectAsState()
    val partialSpeechText by viewModel.partialSpeechText.collectAsState()
    val speechRms by viewModel.speechRms.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    // GitHub & Office Agents State
    val officeAgents = viewModel.officeAgents
    val liveActivities by viewModel.agentLiveActivities.collectAsState()
    val githubRepo by viewModel.githubRepo.collectAsState()
    val githubToken by viewModel.githubToken.collectAsState()
    val githubUserProfile by viewModel.githubUserProfile.collectAsState()
    val userRepositories by viewModel.userRepositories.collectAsState()
    val githubRepoDetails by viewModel.githubRepoDetails.collectAsState()
    val githubIssues by viewModel.githubIssues.collectAsState()
    val isGitHubLoading by viewModel.isGitHubLoading.collectAsState()
    val gitHubStatusMessage by viewModel.gitHubStatusMessage.collectAsState()
    val deviceCodeData by viewModel.deviceCodeResponse.collectAsState()
    val isOAuthPolling by viewModel.isOAuthPolling.collectAsState()
    val isLearningSkill by viewModel.isLearningSkill.collectAsState()
    val learningSkillStatus by viewModel.learningSkillStatus.collectAsState()
    val skillsMasteryList by viewModel.skillsMasteryList.collectAsState()
    val growthSummary by viewModel.growthSummary.collectAsState()
    val aiMoments by viewModel.aiMoments.collectAsState()
    val recentTaskNotification by viewModel.recentTaskNotification.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showSkillsSheet by remember { mutableStateOf(false) }
    var skillsSheetInitialTab by remember { mutableStateOf(0) }
    var showMemorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showOfficeStudioSheet by remember { mutableStateOf(false) }
    var showMomentsSheet by remember { mutableStateOf(false) }
    var commitCodeDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }

    val activeSkill = skills.find { it.id == activeSkillId }

    // Task Completion Notification Toast
    LaunchedEffect(recentTaskNotification) {
        recentTaskNotification?.let { notif ->
            snackbarHostState.showSnackbar(notif)
            viewModel.clearTaskNotification()
        }
    }

    // Live speech stream auto-update into input box
    LaunchedEffect(partialSpeechText) {
        if (isRecording && partialSpeechText.isNotBlank()) {
            inputText = partialSpeechText
        }
    }

    LaunchedEffect(recognizedSpeechText) {
        recognizedSpeechText?.let { speech ->
            inputText = speech
            viewModel.clearRecognizedText()
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Notify error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "需要录音权限以启用语音实时识别功能", Toast.LENGTH_SHORT).show()
        }
    }

    // Pulse animation for recording
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SessionsDrawer(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSelectSession = { id -> viewModel.selectSession(id) },
                onNewSession = { viewModel.createNewSession() },
                onDeleteSession = { session -> viewModel.deleteSession(session) },
                onExportZip = {
                    viewModel.exportCurrentChatToZip { zipFile ->
                        IntentHelper.shareZipFile(context, zipFile)
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Slate900,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nexus 智能助手",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (apiKey.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B))
                                )
                            }
                            Text(
                                text = if (apiKey.isNotBlank()) "引擎: $selectedModel" else "🏛️ CTO.new 自主工程引擎 (免配置)",
                                fontSize = 11.sp,
                                color = if (apiKey.isNotBlank()) Cyan80 else Color(0xFF6EE7B7)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "历史会话",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Office Agents Studio & GitHub Action button
                        IconButton(
                            onClick = { showOfficeStudioSheet = true },
                            modifier = Modifier.testTag("topbar_office_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = "办公室智能体工坊与 GitHub",
                                tint = Color(0xFF38BDF8)
                            )
                        }

                        // AI Moments Feed button
                        IconButton(
                            onClick = { showMomentsSheet = true },
                            modifier = Modifier.testTag("topbar_moments_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DynamicFeed,
                                contentDescription = "AI 朋友圈与开源技能库",
                                tint = ElectricPurple
                            )
                        }

                        // Quick Skills button
                        IconButton(
                            onClick = {
                                skillsSheetInitialTab = 0
                                showSkillsSheet = true
                            },
                            modifier = Modifier.testTag("topbar_skills_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "技能库与 Codex",
                                tint = CyanPrimary
                            )
                        }

                        // AI Growth & Mastery Tracker button
                        IconButton(
                            onClick = {
                                skillsSheetInitialTab = 1
                                showSkillsSheet = true
                            },
                            modifier = Modifier.testTag("topbar_growth_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "AI 助手成长与技能掌握度",
                                    tint = EmeraldGreen
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldGreen,
                                    modifier = Modifier.size(13.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${growthSummary.overallLevel}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        // Long-Term Memory button
                        IconButton(
                            onClick = { showMemorySheet = true },
                            modifier = Modifier.testTag("topbar_memory_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "长期记忆库",
                                tint = ElectricPurple
                            )
                        }

                        // Settings button
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置与密钥",
                                tint = Color(0xFFCBD5E1)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                // Active Skill Banner & Quick Suggestion Prompts
                ActiveSkillBanner(
                    activeSkill = activeSkill,
                    onOpenSkillPicker = { showSkillsSheet = true },
                    onSelectSamplePrompt = { prompt ->
                        inputText = prompt
                    }
                )

                // Office Status Bar: Live Agents & GitHub Activity Matrix
                OfficeStatusBar(
                    officeAgents = officeAgents,
                    liveActivities = liveActivities,
                    currentRepo = githubRepo,
                    currentBranch = githubRepoDetails?.defaultBranch ?: "main",
                    isGitHubLoading = isGitHubLoading,
                    onOpenOfficeStudio = { showOfficeStudioSheet = true },
                    onDispatchAgent = { agent, taskPrompt ->
                        viewModel.dispatchOfficeAgentTask(agent, taskPrompt)
                    },
                    onExecuteQuickCommitPush = { path, content, msg, branch ->
                        viewModel.executeGitCommitAndPush(path, content, msg, branch) { _, resultMsg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(resultMsg)
                            }
                        }
                    }
                )

                // Message List
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (messages.isEmpty()) {
                        // Empty State View
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Nexus 移动端 AI 智能助手就绪",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "搭载 Android 原生语音转文字、Office 多智能体 GitHub 协作、Codex 编程开发与本地长期记忆引擎。",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(messages) { msg ->
                                ChatBubble(
                                    message = msg,
                                    onSpeak = { text ->
                                        if (isSpeaking) {
                                            viewModel.stopSpeaking()
                                        } else {
                                            viewModel.speakText(text)
                                        }
                                    },
                                    onCommitCode = { code, lang ->
                                        commitCodeDialogData = Pair(code, lang)
                                    }
                                )
                            }

                            if (isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = CyanPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Nexus 智能体正在思考与生成...",
                                            fontSize = 13.sp,
                                            color = Cyan80
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Speech Recognition Status Banner
                AnimatedVisibility(
                    visible = isRecording,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = Color(0xFF1E1B4B),
                        border = BorderStroke(1.dp, Color(0xFF6366F1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(if (speechRms > 2f) 1.4f else 1.0f)
                                        .clip(CircleShape)
                                        .background(CoralRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🎙️ 正在实时转文字...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5B4FC)
                                    )
                                    Text(
                                        text = if (partialSpeechText.isNotBlank()) partialSpeechText else "请对准麦克风说话...",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 2
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.stopListening() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "完成录音", tint = Color(0xFF6EE7B7))
                            }
                        }
                    }
                }

                // Bottom Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Native SpeechRecognizer Voice Mic Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(CoralRed.copy(alpha = 0.35f))
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopListening()
                                } else {
                                    val hasAudioPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasAudioPermission) {
                                        viewModel.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) CoralRed else Color(0xFF1E293B))
                                .border(
                                    width = if (isRecording) 1.5.dp else 1.dp,
                                    color = if (isRecording) CoralRed else CyanPrimary.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .testTag("voice_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "停止录音" else "语音输入实时转文字",
                                tint = if (isRecording) Color.White else CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("向 Nexus 提问、按麦克风语音输入或发起 GitHub 协同...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F1523),
                            unfocusedContainerColor = Color(0xFF0F1523)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotEmpty()) {
                                inputText = ""
                                viewModel.sendMessage(textToSend)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) CyanPrimary else Color(0xFF1E293B))
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (inputText.isNotBlank() && !isLoading) Color.Black else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Sheets
    if (commitCodeDialogData != null) {
        CommitCodeToGitHubDialog(
            initialCode = commitCodeDialogData!!.first,
            language = commitCodeDialogData!!.second,
            isAuthenticated = githubToken.isNotBlank(),
            userProfile = githubUserProfile,
            userRepositories = userRepositories,
            defaultRepo = githubRepo,
            deviceCodeResponse = deviceCodeData,
            isOAuthPolling = isOAuthPolling,
            onStartWebOAuth = {
                viewModel.openGitHubOAuthInBrowser(context)
            },
            onStartDeviceOAuth = {
                viewModel.startGitHubDeviceFlow()
            },
            onCancelDeviceOAuth = {
                viewModel.cancelOAuthDeviceFlow()
            },
            onRefreshProfile = {
                viewModel.refreshGitHubUserProfile()
            },
            onCommitCode = { repo, path, code, msg, branch, onResult ->
                viewModel.commitCodeSnippetToGitHub(repo, path, code, msg, branch, onResult)
            },
            onDismiss = {
                commitCodeDialogData = null
            }
        )
    }

    if (showOfficeStudioSheet) {
        OfficeAgentStudioSheet(
            officeAgents = officeAgents,
            currentRepo = githubRepo,
            repoDetails = githubRepoDetails,
            issues = githubIssues,
            userProfile = githubUserProfile,
            userRepositories = userRepositories,
            isGitHubLoading = isGitHubLoading,
            statusMessage = gitHubStatusMessage,
            githubToken = githubToken,
            deviceCodeData = deviceCodeData,
            isOAuthPolling = isOAuthPolling,
            onSaveRepo = { repo ->
                viewModel.saveGitHubRepo(repo)
            },
            onSaveToken = { token ->
                viewModel.saveGitHubToken(token)
                Toast.makeText(context, "GitHub Token 已保存并验证", Toast.LENGTH_SHORT).show()
            },
            onUnbindAccount = {
                viewModel.unbindGitHubAccount()
                Toast.makeText(context, "已解除 GitHub 绑定", Toast.LENGTH_SHORT).show()
            },
            onStartWebOAuth = {
                viewModel.openGitHubOAuthInBrowser(context)
            },
            onStartDeviceOAuth = {
                viewModel.startGitHubDeviceFlow()
            },
            onCancelDeviceOAuth = {
                viewModel.cancelOAuthDeviceFlow()
            },
            onRefreshUserProfile = {
                viewModel.refreshGitHubUserProfile()
            },
            onRefreshRepo = { repo ->
                viewModel.refreshGitHubRepoInfo(repo)
            },
            onCreateIssue = { title, body, labels ->
                viewModel.createIssueOnGitHub(title, body, labels) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onCommitAndPush = { filePath, content, commitMsg, branch, callback ->
                viewModel.executeGitCommitAndPush(filePath, content, commitMsg, branch) { success, msg ->
                    callback(success, msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onCreatePr = { title, head, base, body, callback ->
                viewModel.createPullRequest(title, head, base, body) { success, msg ->
                    callback(success, msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onDispatchAgent = { agent, prompt ->
                viewModel.dispatchOfficeAgentTask(agent, prompt)
            },
            onDismiss = { showOfficeStudioSheet = false }
        )
    }

    if (showSkillsSheet) {
        SkillsSheet(
            skills = skills,
            activeSkillId = activeSkillId,
            skillsMasteryList = skillsMasteryList,
            growthSummary = growthSummary,
            initialTab = skillsSheetInitialTab,
            isLearningSkill = isLearningSkill,
            learningStatus = learningSkillStatus,
            onSelectSkill = { skillId -> viewModel.setActiveSkill(skillId) },
            onPracticeSkill = { skillName, samplePrompt ->
                viewModel.sendMessage(samplePrompt)
            },
            onBoostTraining = { skillId ->
                viewModel.boostSkillTraining(skillId) { newXp ->
                    Toast.makeText(context, "✨ 技能特训成功！已强化 +60 熟练度 XP", Toast.LENGTH_SHORT).show()
                }
            },
            onCreateSkill = { name, desc, prompt, category, samples ->
                viewModel.createCustomSkill(name, desc, prompt, category, samples)
            },
            onLearnFromGitHub = { repoOrUrl ->
                viewModel.learnSkillFromGitHub(repoOrUrl) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            onPublishSkillToGitHub = { skillId ->
                viewModel.publishSkillToGitHub(skillId) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            onOpenAiMoments = {
                showMomentsSheet = true
            },
            onDeleteSkill = { skill -> viewModel.deleteSkill(skill) },
            onDismiss = { showSkillsSheet = false }
        )
    }

    if (showMomentsSheet) {
        AiMomentsHubSheet(
            posts = aiMoments,
            currentRepo = githubRepo,
            skills = skills,
            onToggleLike = { postId -> viewModel.toggleLikeMoment(postId) },
            onDownloadSkillToLibrary = { post ->
                viewModel.downloadSkillFromMoment(post) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onPublishMoment = { title, content, category, skillId, repoUrl ->
                viewModel.publishAiMoment(title, content, category, skillId, repoUrl)
                Toast.makeText(context, "🎉 已发布到 AI 朋友圈与开源共享中心！", Toast.LENGTH_SHORT).show()
            },
            onSharePostToExternal = { post ->
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "【Nexus AI 朋友圈】${post.title}\n${post.content}\n开源地址: ${post.githubRepoUrl ?: "Nexus Agent"}")
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(sendIntent, "分享 AI 动态"))
            },
            onOpenSkillInChat = { skillId, samplePrompt ->
                viewModel.setActiveSkill(skillId)
                viewModel.sendMessage(samplePrompt)
            },
            onDismiss = { showMomentsSheet = false }
        )
    }

    if (showMemorySheet) {
        MemorySheet(
            memories = memories,
            onAddMemory = { key, content -> viewModel.addMemory(key, content) },
            onDeleteMemory = { memory -> viewModel.deleteMemory(memory) },
            onDismiss = { showMemorySheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            currentApiKey = apiKey,
            selectedModel = selectedModel,
            githubUserProfile = githubUserProfile,
            githubRepo = githubRepo,
            onSaveApiKey = { key ->
                viewModel.saveApiKey(key)
                Toast.makeText(context, "API Key Saved", Toast.LENGTH_SHORT).show()
            },
            onSelectModel = { model ->
                viewModel.setModel(model)
            },
            onExportZip = {
                viewModel.exportCurrentChatToZip { zipFile ->
                    IntentHelper.shareZipFile(context, zipFile)
                }
            },
            onOpenGitHubStudio = {
                showSettingsSheet = false
                showOfficeStudioSheet = true
            },
            onStartGitHubOAuth = {
                showSettingsSheet = false
                showOfficeStudioSheet = true
                viewModel.startGitHubDeviceFlow()
            },
            onDismiss = { showSettingsSheet = false }
        )
    }
}
