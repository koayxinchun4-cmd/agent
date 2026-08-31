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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessage
import com.example.ui.theme.AgentBubbleDark
import com.example.ui.theme.AgentBubbleLight
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.UserBubbleDark
import com.example.ui.theme.UserBubbleLight
import com.example.utils.IntentHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessage,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCommitCode: ((code: String, language: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Agent Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(CyanPrimary, ElectricPurple)
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (message.error) Icons.Default.Warning else Icons.Default.SmartToy,
                        contentDescription = "Nexus Agent",
                        tint = if (message.error) CoralRed else CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Card
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .testTag(if (isUser) "user_message_bubble" else "agent_message_bubble"),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.error -> CoralRed.copy(alpha = 0.15f)
                    isUser -> UserBubbleDark
                    else -> AgentBubbleDark
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    message.error -> CoralRed.copy(alpha = 0.4f)
                    isUser -> CyanPrimary.copy(alpha = 0.25f)
                    else -> Color(0xFF26334D)
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header (Skill tag, Sender, Time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isUser) "你" else "Nexus 智能体",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Cyan80 else CyanPrimary
                        )

                        if (!isUser && message.skillNameUsed != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ElectricPurple.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = message.skillNameUsed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE9D5FF),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Body content
                if (isUser) {
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                } else {
                    MarkdownViewer(
                        content = message.content,
                        textColor = if (message.error) CoralRed else Color(0xFFE2E8F0),
                        onCommitCode = onCommitCode
                    )
                }

                // Action Bar for AI response
                if (!isUser && !message.error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.tokenCount != null && message.tokenCount > 0) {
                            Text(
                                text = "${message.tokenCount} 个 Token",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Read aloud TTS
                        IconButton(
                            onClick = { onSpeak(message.content) },
                            modifier = Modifier.size(28.dp).testTag("speak_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "语音朗读",
                                tint = Cyan80,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                IntentHelper.copyToClipboard(context, "Nexus 回复内容", message.content)
                            },
                            modifier = Modifier.size(28.dp).testTag("copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "复制文本",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                IntentHelper.shareText(context, "Nexus 智能助手回答", message.content)
                            },
                            modifier = Modifier.size(28.dp).testTag("share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Contextual Action Shortcuts based on detected type
                    if (message.actionType == "office") {
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            SuggestionChip(
                                onClick = {
                                    IntentHelper.openEmailDraft(context, "Nexus 办公报告", message.content)
                                },
                                label = { Text("起草邮件", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = ElectricPurple
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = ElectricPurple.copy(alpha = 0.15f),
                                    labelColor = Color(0xFFE9D5FF)
                                )
                            )
                        }
                    } else if (message.actionType == "auto") {
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            SuggestionChip(
                                onClick = {
                                    IntentHelper.openCalendarEvent(context, "Nexus 日程提醒", message.content)
                                },
                                label = { Text("添加至系统日历", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = EmeraldGreen
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = EmeraldGreen.copy(alpha = 0.15f),
                                    labelColor = Color(0xFF6EE7B7)
                                )
                            )
                        }
                    } else if (message.actionType == "code" || message.content.contains("```")) {
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            SuggestionChip(
                                onClick = {
                                    val extracted = extractFirstCodeSnippet(message.content)
                                    onCommitCode?.invoke(extracted.first, extracted.second)
                                },
                                label = { Text("提交代码至 GitHub 仓库", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = CyanPrimary
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = CyanPrimary.copy(alpha = 0.15f),
                                    labelColor = Cyan80
                                )
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Cyan80,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun extractFirstCodeSnippet(markdown: String): Pair<String, String> {
    val startIdx = markdown.indexOf("```")
    if (startIdx != -1) {
        val langEndIdx = markdown.indexOf("\n", startIdx)
        if (langEndIdx != -1) {
            val language = markdown.substring(startIdx + 3, langEndIdx).trim()
            val endIdx = markdown.indexOf("```", langEndIdx)
            if (endIdx != -1) {
                val code = markdown.substring(langEndIdx + 1, endIdx).trim()
                return Pair(code, language)
            }
        }
    }
    return Pair(markdown, "text")
}
