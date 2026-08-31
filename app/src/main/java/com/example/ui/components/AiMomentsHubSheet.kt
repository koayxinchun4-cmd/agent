package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AgentSkill
import com.example.data.local.AiMomentPost
import com.example.data.local.PostCategory
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiMomentsHubSheet(
    posts: List<AiMomentPost>,
    currentRepo: String,
    skills: List<AgentSkill>,
    onToggleLike: (String) -> Unit,
    onDownloadSkillToLibrary: (AiMomentPost) -> Unit,
    onPublishMoment: (title: String, content: String, category: String, skillId: String?, repoUrl: String?) -> Unit,
    onSharePostToExternal: (AiMomentPost) -> Unit,
    onOpenSkillInChat: (skillId: String, samplePrompt: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryFilter by remember { mutableStateOf<String?>("ALL") }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    // Publish form states
    var postTitle by remember { mutableStateOf("") }
    var postContent by remember { mutableStateOf("") }
    var postCategory by remember { mutableStateOf("SKILL_SHARE") }
    var selectedSkillForShare by remember { mutableStateOf<AgentSkill?>(skills.firstOrNull()) }
    var postRepoUrl by remember { mutableStateOf("https://github.com/$currentRepo") }

    val filteredPosts = remember(posts, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == null) {
            posts
        } else {
            posts.filter { it.category == selectedCategoryFilter }
        }
    }

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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricPurple, CyanPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DynamicFeed,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI 朋友圈 & 技能公开中心",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "分享自主进化心得 · GitHub 公开仓库与技能一键下载",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showCreatePostDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.testTag("publish_moment_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("发朋友圈", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "ALL",
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = { Text("全部动态 (${posts.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "SKILL_SHARE",
                        onClick = { selectedCategoryFilter = "SKILL_SHARE" },
                        label = { Text("✨ 技能分享", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "GITHUB_RELEASE",
                        onClick = { selectedCategoryFilter = "GITHUB_RELEASE" },
                        label = { Text("🚀 GitHub 开源", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.Black,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "TASK_ACCOMPLISHED",
                        onClick = { selectedCategoryFilter = "TASK_ACCOMPLISHED" },
                        label = { Text("⚡ 实战突破", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Moments Feed List
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无此类动态，点击右上角「发朋友圈」发布第一条吧！", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        AiMomentCard(
                            post = post,
                            onLikeClick = { onToggleLike(post.id) },
                            onDownloadClick = { onDownloadSkillToLibrary(post) },
                            onShareClick = { onSharePostToExternal(post) },
                            onOpenRepoClick = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            onUseSkillClick = { skillId ->
                                onOpenSkillInChat(skillId, "请使用【${post.relatedSkillName ?: "此技能"}】帮我执行任务并进行实战演示。")
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }

    // Publish New AI Moment Dialog
    if (showCreatePostDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCreatePostDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCardBg,
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("发布 AI 朋友圈与开源技能", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { showCreatePostDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("动态类型", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "SKILL_SHARE" to "✨ 技能分享",
                            "GITHUB_RELEASE" to "🚀 GitHub 发布",
                            "TASK_ACCOMPLISHED" to "⚡ 实战突破"
                        ).forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = postCategory == catKey,
                                onClick = { postCategory = catKey },
                                label = { Text(catLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = Slate900,
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("动态标题") },
                        placeholder = { Text("例如：刚刚在 GitHub 成功发布了 Python 爬虫技能") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("post_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("心得与说明") },
                        placeholder = { Text("记录智能体进化历程，或公开开源仓库的使用方式...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth().testTag("post_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = postRepoUrl,
                        onValueChange = { postRepoUrl = it },
                        label = { Text("关联 GitHub 仓库公开地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("post_repo_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showCreatePostDialog = false }) {
                            Text("取消", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (postTitle.isNotBlank() && postContent.isNotBlank()) {
                                    onPublishMoment(
                                        postTitle.trim(),
                                        postContent.trim(),
                                        postCategory,
                                        selectedSkillForShare?.id,
                                        postRepoUrl.trim().ifBlank { null }
                                    )
                                    showCreatePostDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.testTag("submit_publish_moment")
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("公开广播", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiMomentCard(
    post: AiMomentPost,
    onLikeClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenRepoClick: (String) -> Unit,
    onUseSkillClick: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val timeFormatted = remember(post.timestamp) {
        val diffSec = (System.currentTimeMillis() - post.timestamp) / 1000
        when {
            diffSec < 60 -> "刚刚"
            diffSec < 3600 -> "${diffSec / 60} 分钟前"
            diffSec < 86400 -> "${diffSec / 3600} 小时前"
            else -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(post.timestamp))
        }
    }

    val (badgeBg, badgeText, badgeColor) = when (post.category) {
        "GITHUB_RELEASE" -> Triple(EmeraldGreen.copy(alpha = 0.15f), "🚀 GitHub 开源", EmeraldGreen)
        "SKILL_SHARE" -> Triple(ElectricPurple.copy(alpha = 0.15f), "✨ 技能发布", ElectricPurple)
        "TASK_ACCOMPLISHED" -> Triple(CyanPrimary.copy(alpha = 0.15f), "⚡ 实战突破", CyanPrimary)
        else -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), "💡 智能体心声", Color(0xFFF59E0B))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author & Time Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricPurple, CyanPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = post.authorRole,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Title & Content
            Text(
                text = post.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            // GitHub Public Repo Link & Download Area
            if (!post.githubRepoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "公开开源仓库",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = post.githubRepoName ?: post.githubRepoUrl!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy clone command
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("git clone ${post.githubRepoUrl}.git"))
                                    android.widget.Toast.makeText(context, "已复制 Git 克隆指令！", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制克隆命令", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }

                            // Open in Browser
                            IconButton(
                                onClick = { onOpenRepoClick(post.githubRepoUrl!!) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = "浏览器打开", tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    post.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            color = Cyan80,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Footer (Like, Download/Import, Practice, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (post.isLikedByMe) CoralRed else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 12.sp,
                        color = if (post.isLikedByMe) CoralRed else Color(0xFF94A3B8)
                    )
                }

                // Download/Import or Practice Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (post.relatedSkillId != null) {
                        Button(
                            onClick = { onUseSkillClick(post.relatedSkillId) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary.copy(alpha = 0.15f)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("调用实战", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDownloadClick,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple.copy(alpha = 0.2f)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("一键下载 (${post.downloadCount})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "分享", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
