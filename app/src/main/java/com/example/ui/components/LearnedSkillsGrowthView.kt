package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AgentSkill
import com.example.data.local.AssistantGrowthSummary
import com.example.data.local.MasteryRank
import com.example.data.local.SkillMasteryItem
import com.example.data.local.SkillOriginType
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import org.json.JSONArray

@Composable
fun LearnedSkillsGrowthView(
    growthSummary: AssistantGrowthSummary,
    masteryItems: List<SkillMasteryItem>,
    activeSkillId: String?,
    onSelectSkill: (String) -> Unit,
    onPracticeSkill: (skillName: String, samplePrompt: String) -> Unit,
    onBoostTraining: (skillId: String) -> Unit,
    onLearnFromGitHubClick: () -> Unit,
    onPublishToGitHub: (skillId: String) -> Unit,
    onOpenAiMoments: () -> Unit,
    onDeleteSkill: (AgentSkill) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(masteryItems, selectedFilter, searchQuery) {
        masteryItems.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "GITHUB" -> item.originType == SkillOriginType.GITHUB_LEARNED
                "EXPERT" -> item.masteryRank.levelNumber >= 4
                "IN_PROGRESS" -> item.masteryRank.levelNumber < 4
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.skill.name.contains(searchQuery, ignoreCase = true) ||
                    item.skill.description.contains(searchQuery, ignoreCase = true) ||
                    item.skill.category.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("learned_skills_growth_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Assistant Growth Hero Dashboard Card
        item {
            GrowthHeroCard(
                summary = growthSummary,
                onLearnGitHub = onLearnFromGitHubClick,
                onOpenMoments = onOpenAiMoments
            )
        }

        // 2. Capability Domains Matrix Card
        item {
            CapabilityDomainsCard(
                categoryProficiency = growthSummary.categoryProficiency
            )
        }

        // 3. Search & Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索已掌握技能或关键词...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF26354D),
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_skills_search_input")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val githubCount = masteryItems.count { it.originType == SkillOriginType.GITHUB_LEARNED }
                    val expertCount = masteryItems.count { it.masteryRank.levelNumber >= 4 }
                    val inProgressCount = masteryItems.count { it.masteryRank.levelNumber < 4 }

                    item {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("全部 (${masteryItems.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            ),
                            modifier = Modifier.testTag("filter_all_skills")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "GITHUB",
                            onClick = { selectedFilter = "GITHUB" },
                            label = { Text("🧬 GitHub 习得 ($githubCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple.copy(alpha = 0.25f),
                                selectedLabelColor = ElectricPurple
                            ),
                            modifier = Modifier.testTag("filter_github_skills")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "EXPERT",
                            onClick = { selectedFilter = "EXPERT" },
                            label = { Text("🏆 精通/宗师 ($expertCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag("filter_expert_skills")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "IN_PROGRESS",
                            onClick = { selectedFilter = "IN_PROGRESS" },
                            label = { Text("⚡ 实践进阶中 ($inProgressCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("filter_inprogress_skills")
                        )
                    }
                }
            }
        }

        // 4. Skills Mastery Cards List
        if (filteredItems.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCardBg,
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "没有找到匹配的技能" else "暂无该分类的习得技能",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "输入任何 GitHub 仓库即可让 AI 助手深度自学进化！",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = onLearnFromGitHubClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            modifier = Modifier.testTag("empty_learn_github_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("立即从 GitHub 导入新技能", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.skill.id }) { item ->
                SkillMasteryCard(
                    item = item,
                    isActive = item.skill.id == activeSkillId,
                    onSelectSkill = { onSelectSkill(item.skill.id) },
                    onPractice = { prompt -> onPracticeSkill(item.skill.name, prompt) },
                    onBoostTraining = { onBoostTraining(item.skill.id) },
                    onPublishToGitHub = { onPublishToGitHub(item.skill.id) },
                    onDelete = { onDeleteSkill(item.skill) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GrowthHeroCard(
    summary: AssistantGrowthSummary,
    onLearnGitHub: () -> Unit,
    onOpenMoments: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.overallProgress,
        animationSpec = tween(durationMillis = 800),
        label = "overallProgress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.6f), ElectricPurple.copy(alpha = 0.6f)))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("growth_hero_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Assistant Level & Rank Badge + Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyanPrimary.copy(alpha = 0.2f), ElectricPurple.copy(alpha = 0.4f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "成长徽章",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lv.${summary.overallLevel}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = summary.rankTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "全域多模态认知与自主进化引擎",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onOpenMoments() }.testTag("hero_open_moments_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DynamicFeed, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI朋友圈", fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricPurple.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onLearnGitHub() }.testTag("hero_learn_github_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GitHub 进化", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Experience Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "形态进化经验 (Total XP)",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "${summary.totalXp} / ${summary.targetNextLevelXp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cyan80
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyanPrimary,
                    trackColor = Color(0xFF1E293B)
                )

                Text(
                    text = "距下一形态晋升还需 ${maxOf(0, summary.targetNextLevelXp - summary.totalXp)} XP · 每次实战与特训均可积累",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            // 4-Grid Metrics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrowthStatTile(
                    label = "掌握技能",
                    value = "${summary.totalSkillsCount} 项",
                    icon = Icons.Default.Psychology,
                    tint = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                GrowthStatTile(
                    label = "GitHub 进化",
                    value = "${summary.githubSkillsCount} 项",
                    icon = Icons.Default.School,
                    tint = ElectricPurple,
                    modifier = Modifier.weight(1f)
                )
                GrowthStatTile(
                    label = "实战推演",
                    value = "${summary.totalPracticesCount} 次",
                    icon = Icons.Default.Bolt,
                    tint = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                GrowthStatTile(
                    label = "综合掌握率",
                    value = "${summary.masteryRatePercent}%",
                    icon = Icons.Default.TrendingUp,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GrowthStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(0.5.dp, Color(0xFF26354D)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun CapabilityDomainsCard(
    categoryProficiency: Map<String, Float>
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCardBg,
        border = BorderStroke(1.dp, Color(0xFF26354D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "能力领域熟练度图谱",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "多维演进体系",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            val codingProgress = categoryProficiency["coding"] ?: 0.85f
            val officeProgress = categoryProficiency["office"] ?: 0.78f
            val autoProgress = categoryProficiency["automation"] ?: 0.72f
            val coreProgress = categoryProficiency["productivity"] ?: 0.90f

            CapabilityBar(
                title = "💻 编程开发与架构 (Coding & PRs)",
                progress = codingProgress,
                barColor = CyanPrimary
            )
            CapabilityBar(
                title = "📝 办公协同与文档 (Office & PRD)",
                progress = officeProgress,
                barColor = ElectricPurple
            )
            CapabilityBar(
                title = "⚙️ 系统工程与自动化 (CI/CD & Shell)",
                progress = autoProgress,
                barColor = EmeraldGreen
            )
            CapabilityBar(
                title = "🧠 核心推理与自主迁移 (Core AI & Logic)",
                progress = coreProgress,
                barColor = Color(0xFF38BDF8)
            )
        }
    }
}

@Composable
private fun CapabilityBar(
    title: String,
    progress: Float,
    barColor: Color
) {
    val animatedProg by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "capProgress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        LinearProgressIndicator(
            progress = { animatedProg },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun SkillMasteryCard(
    item: SkillMasteryItem,
    isActive: Boolean,
    onSelectSkill: () -> Unit,
    onPractice: (String) -> Unit,
    onBoostTraining: () -> Unit,
    onPublishToGitHub: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rankColor = Color(item.masteryRank.badgeColorHex)
    val originColor = Color(item.originType.badgeColorHex)

    val samplePrompts = remember(item.skill.samplePromptsJson) {
        try {
            val arr = JSONArray(item.skill.samplePromptsJson)
            List(arr.length()) { i -> arr.getString(i) }
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF132338) else DarkCardBg
        ),
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) CyanPrimary else Color(0xFF26354D)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skill_mastery_card_${item.skill.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Skill Icon + Name + Badges + Active Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(originColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                item.originType == SkillOriginType.GITHUB_LEARNED -> Icons.Default.School
                                item.skill.category == "coding" -> Icons.Default.Code
                                item.skill.category == "office" -> Icons.Default.Description
                                item.skill.category == "automation" -> Icons.Default.SettingsSuggest
                                else -> Icons.Default.SmartToy
                            },
                            contentDescription = item.skill.name,
                            tint = originColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.skill.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Origin Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = originColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = item.originType.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = originColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = item.skill.description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = if (isExpanded) 4 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Mastery Rank Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rankColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, rankColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = rankColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Lv.${item.masteryRank.levelNumber} ${item.masteryRank.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = rankColor
                        )
                    }
                }
            }

            // Mastery Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "熟练度: ${(item.progress * 100).toInt()}% (${item.masteryRank.description})",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "${item.currentXp} / ${item.targetNextXp} XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = item.progress,
                    animationSpec = tween(500),
                    label = "skillProgress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = rankColor,
                    trackColor = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎯 已实战演练 ${item.practiceCount} 次 · ${item.lastPracticedDesc}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = if (isExpanded) "收起规范 ▲" else "查看规则 ▼",
                        fontSize = 10.sp,
                        color = Cyan80,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            }

            // Expandable Detail / System Prompt / Sample Prompts
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(0.5.dp, Color(0xFF26354D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "【核心 System 指令规范】",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.skill.systemPrompt,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 15.sp,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (samplePrompts.isNotEmpty()) {
                        Text(
                            text = "💡 推荐演练指令（点击立即实战）：",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                        samplePrompts.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectSkill()
                                        onPractice(prompt)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prompt,
                                        fontSize = 11.sp,
                                        color = Cyan80,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "演练",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Boost Training Button (+60 XP)
                OutlinedButton(
                    onClick = onBoostTraining,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("boost_skill_${item.skill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "特训",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("+60XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Publish to GitHub Button
                IconButton(
                    onClick = onPublishToGitHub,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.15f))
                        .testTag("publish_github_skill_${item.skill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Publish,
                        contentDescription = "发布至 GitHub",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Activate Button
                Button(
                    onClick = onSelectSkill,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) EmeraldGreen else CyanPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("activate_skill_${item.skill.id}")
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Check else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isActive) "已激活为当前技能" else "设为活跃技能",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!item.skill.isBuiltIn) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_skill_${item.skill.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除技能",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
