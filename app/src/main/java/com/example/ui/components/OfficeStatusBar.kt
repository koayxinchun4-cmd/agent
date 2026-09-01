package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AgentActivityType
import com.example.data.api.AgentLiveActivity
import com.example.data.api.OfficeAgent
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

/**
 * 办公室状态栏 UI 组件 (Office Status Bar)
 * 实时展示当前活跃的 AI Agent 列表及其在 GitHub 上的实时活动状态（如正在提交、推送代码、审查 PR、巡检 Issue 等）
 */
@Composable
fun OfficeStatusBar(
    officeAgents: List<OfficeAgent>,
    liveActivities: Map<String, AgentLiveActivity>,
    currentRepo: String,
    currentBranch: String,
    isGitHubLoading: Boolean,
    onOpenOfficeStudio: () -> Unit,
    onDispatchAgent: (OfficeAgent, String) -> Unit,
    onExecuteQuickCommitPush: (String, String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedAgentForDetail by remember { mutableStateOf<OfficeAgent?>(null) }
    var showQuickDispatchDialog by remember { mutableStateOf(false) }
    var quickTaskPrompt by remember { mutableStateOf("") }

    // Pulsing heartbeat animation for active agents indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val activeCount = liveActivities.values.count { it.isBusy || it.activityType != AgentActivityType.IDLE }

    Surface(
        color = Color(0xFF0B1120),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .testTag("office_status_bar")
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            // Header Bar: Title, Repo Pill, Active Indicator, Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 2.dp)
                ) {
                    // Pulsing Status Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(if (activeCount > 0 || isGitHubLoading) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (isGitHubLoading) Color(0xFFF59E0B)
                                else if (activeCount > 0) Color(0xFF38BDF8)
                                else Color(0xFF10B981)
                            )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "办公室状态栏",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Active Count Tag
                    Surface(
                        color = if (activeCount > 0) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isGitHubLoading) "⚡ GitHub 任务执行中"
                            else if (activeCount > 0) "● $activeCount 位正在活跃"
                            else "● 5位全员待命",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGitHubLoading) Color(0xFFFBBF24)
                            else if (activeCount > 0) Color(0xFF38BDF8)
                            else Color(0xFF6EE7B7),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // GitHub Repo & Branch Badge
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onOpenOfficeStudio() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🐙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currentRepo.substringAfterLast("/").ifBlank { currentRepo },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Cyan80,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ":$currentBranch",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Expand / Collapse Chevron Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "折叠" else "展开全景",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Horizontal Scrollable Active Agent Chips Reel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(officeAgents) { agent ->
                    val activity = liveActivities[agent.id] ?: AgentLiveActivity(
                        agentId = agent.id,
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "待命就绪",
                        targetRepo = currentRepo,
                        targetBranch = currentBranch
                    )

                    AgentStatusChip(
                        agent = agent,
                        activity = activity,
                        onClick = {
                            selectedAgentForDetail = agent
                        }
                    )
                }
            }

            // Expanded Panoramic Detail Panel (全景展开详情视图)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFF1E293B))
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI 智能体矩阵 • GitHub 实时协同监控",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )

                        TextButton(
                            onClick = onOpenOfficeStudio,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("打开完整作战室", fontSize = 11.sp, color = CyanPrimary)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 5 Agents Detailed Rows
                    officeAgents.forEach { agent ->
                        val activity = liveActivities[agent.id] ?: AgentLiveActivity(
                            agentId = agent.id,
                            activityType = AgentActivityType.IDLE,
                            targetRepo = currentRepo,
                            targetBranch = currentBranch
                        )

                        AgentExpandedRow(
                            agent = agent,
                            activity = activity,
                            onDispatch = {
                                selectedAgentForDetail = agent
                                quickTaskPrompt = agent.defaultPromptTemplate.replace("%REPO%", currentRepo)
                                showQuickDispatchDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog for Individual Agent Activity Details & Quick Action
    if (selectedAgentForDetail != null) {
        val agent = selectedAgentForDetail!!
        val activity = liveActivities[agent.id] ?: AgentLiveActivity(
            agentId = agent.id,
            activityType = AgentActivityType.IDLE,
            targetRepo = currentRepo,
            targetBranch = currentBranch
        )

        AgentActivityDetailDialog(
            agent = agent,
            activity = activity,
            currentRepo = currentRepo,
            currentBranch = currentBranch,
            onDismiss = { selectedAgentForDetail = null },
            onDispatchTask = { prompt ->
                onDispatchAgent(agent, prompt)
                selectedAgentForDetail = null
            },
            onQuickCommitPush = {
                onExecuteQuickCommitPush(
                    "README.md",
                    "# Auto-updated by ${agent.name}\n\nSynced at ${System.currentTimeMillis()}",
                    "chore(agent): automated sync by ${agent.name}",
                    currentBranch
                )
                selectedAgentForDetail = null
            },
            onOpenStudio = {
                selectedAgentForDetail = null
                onOpenOfficeStudio()
            }
        )
    }

    // Quick Dispatch Task Dialog
    if (showQuickDispatchDialog && selectedAgentForDetail != null) {
        val agent = selectedAgentForDetail!!
        AlertDialog(
            onDismissRequest = { showQuickDispatchDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(agent.avatarEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("与 ${agent.name} 开启协同", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "目标仓库: $currentRepo (分支: $currentBranch)",
                        fontSize = 11.sp,
                        color = Cyan80
                    )
                    OutlinedTextField(
                        value = quickTaskPrompt,
                        onValueChange = { quickTaskPrompt = it },
                        label = { Text("任务指令", fontSize = 11.sp) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDispatchAgent(agent, quickTaskPrompt)
                        showQuickDispatchDialog = false
                        selectedAgentForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    enabled = quickTaskPrompt.isNotBlank()
                ) {
                    Text("立即派遣", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickDispatchDialog = false }) {
                    Text("取消", color = Color.Gray)
                }
            }
        )
    }
}

/**
 * Compact Status Chip displayed in the horizontal reel
 */
@Composable
private fun AgentStatusChip(
    agent: OfficeAgent,
    activity: AgentLiveActivity,
    onClick: () -> Unit
) {
    val badgeColor = Color(activity.activityType.badgeColorHex)
    val isBusy = activity.isBusy || activity.activityType != AgentActivityType.IDLE

    Surface(
        color = if (isBusy) Color(0xFF131E35) else Slate800,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isBusy) badgeColor.copy(alpha = 0.8f) else Color(0xFF334155)
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("agent_status_chip_${agent.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Emoji in Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(agent.avatarEmoji, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = agent.name.take(4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                }

                // Activity status label
                Text(
                    text = if (isBusy) activity.activityType.label else "就绪待命",
                    fontSize = 9.sp,
                    color = if (isBusy) badgeColor else Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Expanded full row for an agent in panoramic view
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AgentExpandedRow(
    agent: OfficeAgent,
    activity: AgentLiveActivity,
    onDispatch: () -> Unit
) {
    val badgeColor = Color(activity.activityType.badgeColorHex)
    val isBusy = activity.isBusy || activity.activityType != AgentActivityType.IDLE

    Surface(
        color = Slate900,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, if (isBusy) badgeColor.copy(alpha = 0.5f) else Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(agent.avatarEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(agent.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(${agent.roleTitle})", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(badgeColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activity.activityType.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Real-time activity log line
            Text(
                text = "⚡ ${activity.activityTitle} • ${activity.detailLog}",
                fontSize = 10.sp,
                color = if (isBusy) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isBusy && activity.progress > 0f) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { activity.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = badgeColor,
                    trackColor = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分支: ${activity.targetBranch}",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )

                Button(
                    onClick = onDispatch,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("开启协同", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Detailed Dialog popup when tapping an Agent Chip
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AgentActivityDetailDialog(
    agent: OfficeAgent,
    activity: AgentLiveActivity,
    currentRepo: String,
    currentBranch: String,
    onDismiss: () -> Unit,
    onDispatchTask: (String) -> Unit,
    onQuickCommitPush: () -> Unit,
    onOpenStudio: () -> Unit
) {
    var taskInput by remember {
        mutableStateOf(agent.defaultPromptTemplate.replace("%REPO%", currentRepo))
    }
    val badgeColor = Color(activity.activityType.badgeColorHex)
    val isBusy = activity.isBusy || activity.activityType != AgentActivityType.IDLE

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
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
                            .background(badgeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(agent.avatarEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(agent.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(agent.roleTitle, fontSize = 11.sp, color = Cyan80)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Status Box
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GitHub 实时活动状态", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Surface(color = badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = activity.activityType.label,
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "动作: ${activity.activityTitle}",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "详情: ${activity.detailLog}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("目标仓库: $currentRepo", fontSize = 9.sp, color = Color(0xFF64748B))
                            Text("分支: $currentBranch", fontSize = 9.sp, color = Color(0xFF64748B))
                        }

                        if (isBusy && activity.progress > 0f) {
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { activity.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = badgeColor,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                // Capabilities Pills
                Text("核心专业能力：", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    agent.capabilities.forEach { cap ->
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "• $cap",
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Prompt Input Box
                OutlinedTextField(
                    value = taskInput,
                    onValueChange = { taskInput = it },
                    label = { Text("协同指令 (即时联动)", fontSize = 11.sp) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    )
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenStudio,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, Cyan80)
                ) {
                    Text("前往工坊", color = Cyan80, fontSize = 11.sp)
                }

                Button(
                    onClick = { onDispatchTask(taskInput) },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    enabled = taskInput.isNotBlank()
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开启协同", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        dismissButton = {}
    )
}
