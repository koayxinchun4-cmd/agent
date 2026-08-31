package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AgentSkill
import com.example.data.local.AssistantGrowthSummary
import com.example.data.local.SkillMasteryItem
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan80
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsSheet(
    skills: List<AgentSkill>,
    activeSkillId: String?,
    skillsMasteryList: List<SkillMasteryItem> = emptyList(),
    growthSummary: AssistantGrowthSummary? = null,
    initialTab: Int = 0,
    isLearningSkill: Boolean = false,
    learningStatus: String? = null,
    onSelectSkill: (String) -> Unit,
    onPracticeSkill: (skillName: String, samplePrompt: String) -> Unit = { _, _ -> },
    onBoostTraining: (skillId: String) -> Unit = {},
    onCreateSkill: (name: String, desc: String, prompt: String, category: String, samples: List<String>) -> Unit,
    onLearnFromGitHub: (repoOrUrl: String) -> Unit = {},
    onPublishSkillToGitHub: (skillId: String) -> Unit = {},
    onOpenAiMoments: () -> Unit = {},
    onDeleteSkill: (AgentSkill) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentTab by remember { mutableStateOf(initialTab) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showLearnGitHubDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "智能体技能库与自主进化",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "支持从 GitHub 仓库或 SKILL.md 一键自主学习进化",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showLearnGitHubDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        modifier = Modifier.testTag("learn_github_skill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "GitHub 学习",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GitHub 学习", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.testTag("add_custom_skill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新建",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新建", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs (Skills List vs Growth & Mastery Visualization)
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = Color(0xFF0F1524),
                contentColor = CyanPrimary,
                divider = {},
                indicator = { tabPositions ->
                    if (currentTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                            color = if (currentTab == 1) CyanPrimary else ElectricPurple,
                            height = 3.dp
                        )
                    }
                }
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("技能库与自学 (${skills.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_skills_library")
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI 成长掌握度 (${growthSummary?.overallLevel ?: 1}级)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (currentTab == 1) CyanPrimary else Color(0xFF94A3B8)
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_skills_growth")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLearningSkill || learningStatus != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricPurple.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLearningSkill) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ElectricPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = learningStatus ?: "正在从 GitHub 深度学习技能...",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (currentTab == 1) {
                // Growth & Mastery View
                LearnedSkillsGrowthView(
                    growthSummary = growthSummary ?: AssistantGrowthSummary(1, "初阶智能体", 0, 800, 0f, skills.size, 0, 0, 0),
                    masteryItems = skillsMasteryList,
                    activeSkillId = activeSkillId,
                    onSelectSkill = { skillId ->
                        onSelectSkill(skillId)
                    },
                    onPracticeSkill = { skillName, samplePrompt ->
                        onPracticeSkill(skillName, samplePrompt)
                        onDismiss()
                    },
                    onBoostTraining = onBoostTraining,
                    onLearnFromGitHubClick = {
                        showLearnGitHubDialog = true
                    },
                    onPublishToGitHub = onPublishSkillToGitHub,
                    onOpenAiMoments = {
                        onDismiss()
                        onOpenAiMoments()
                    },
                    onDeleteSkill = onDeleteSkill,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                // Skills List
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                items(skills) { skill ->
                    val isSelected = skill.id == activeSkillId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectSkill(skill.id)
                                onDismiss()
                            }
                            .testTag("skill_item_${skill.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF19253B) else Color(0xFF111726)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) CyanPrimary else Color(0xFF222F46)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            skill.id.startsWith("gh_skill_") -> ElectricPurple.copy(alpha = 0.25f)
                                            skill.category == "office" -> ElectricPurple.copy(alpha = 0.2f)
                                            skill.category == "automation" -> EmeraldGreen.copy(alpha = 0.2f)
                                            skill.category == "coding" -> CyanPrimary.copy(alpha = 0.2f)
                                            else -> Cyan80.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (skill.id.startsWith("gh_skill_")) Icons.Default.School else getSkillIcon(skill.iconName),
                                    contentDescription = skill.name,
                                    tint = when {
                                        skill.id.startsWith("gh_skill_") -> ElectricPurple
                                        skill.category == "office" -> ElectricPurple
                                        skill.category == "automation" -> EmeraldGreen
                                        skill.category == "coding" -> CyanPrimary
                                        else -> Cyan80
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = skill.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    if (skill.id.startsWith("gh_skill_")) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ElectricPurple.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "GitHub 习得",
                                                fontSize = 9.sp,
                                                color = ElectricPurple,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (!skill.isBuiltIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = CyanPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "自定义",
                                                fontSize = 9.sp,
                                                color = CyanPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = skill.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 16.sp
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选择",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (!skill.isBuiltIn) {
                                IconButton(
                                    onClick = { onDeleteSkill(skill) },
                                    modifier = Modifier.size(28.dp).testTag("delete_custom_skill_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = CoralRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLearnGitHubDialog) {
        LearnGitHubSkillDialog(
            onDismiss = { showLearnGitHubDialog = false },
            onLearn = { urlOrRepo ->
                onLearnFromGitHub(urlOrRepo)
                showLearnGitHubDialog = false
            }
        )
    }

    if (showCreateDialog) {
        CreateSkillDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, prompt, category, samples ->
                onCreateSkill(name, desc, prompt, category, samples)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun LearnGitHubSkillDialog(
    onDismiss: () -> Unit,
    onLearn: (String) -> Unit
) {
    var repoOrUrl by remember { mutableStateOf("https://github.com/") }

    val presetSkills = listOf(
        "google/mesop (UI Web 框架)" to "google/mesop/README.md",
        "Android Room DB 规范" to "android/architecture-samples/README.md",
        "Python FastAPI 后端" to "tiangolo/fastapi/README.md",
        "Docker CI/CD 编排" to "docker/awesome-compose/README.md"
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF26354D)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GitHub 技能自主学习与导入",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                    }
                }

                Text(
                    text = "输入任何 GitHub 仓库、SKILL.md 或文档链接，Agent 将通过 Gemini 语义理解深度提炼出专业 System Prompt、执行约束与示例，并立即注入智能体技能库！",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = repoOrUrl,
                    onValueChange = { repoOrUrl = it },
                    label = { Text("GitHub 仓库或 SKILL.md 链接") },
                    placeholder = { Text("例: owner/repo 或 https://github.com/...") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("github_skill_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text(
                    text = "热门快速学习推荐：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetSkills.forEach { (label, presetVal) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(0.5.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repoOrUrl = presetVal
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 12.sp, color = Cyan80)
                                Text("填入", fontSize = 11.sp, color = ElectricPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (repoOrUrl.isNotBlank()) {
                            onLearn(repoOrUrl.trim())
                        }
                    },
                    enabled = repoOrUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("start_learn_github_skill_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("开始提取并深度学习技能", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreateSkillDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, prompt: String, category: String, samples: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("custom") }
    var samplePrompt1 by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF26354D)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
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
                    Text(
                        text = "新建自定义技能",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("技能名称 (如：SQL 专家、日程规划师)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("skill_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("简短介绍") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("skill_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("系统人设与指令 (System Instruction)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("skill_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = samplePrompt1,
                    onValueChange = { samplePrompt1 = it },
                    label = { Text("示例触发提示词 (Sample Prompt)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("skill_sample_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        if (name.isNotBlank() && systemPrompt.isNotBlank()) {
                            val samples = if (samplePrompt1.isNotBlank()) listOf(samplePrompt1) else emptyList()
                            onCreate(name, description, systemPrompt, category, samples)
                        }
                    },
                    enabled = name.isNotBlank() && systemPrompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("save_custom_skill_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("保存并激活技能", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
