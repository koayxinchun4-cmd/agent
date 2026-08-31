package com.example.data.local

enum class SkillOriginType(val label: String, val badgeColorHex: Long) {
    GITHUB_LEARNED("GitHub 习得", 0xFFA855F7),
    BUILT_IN("核心内置", 0xFF00E5FF),
    CUSTOM("自建技能", 0xFF10B981)
}

enum class MasteryRank(val title: String, val levelNumber: Int, val badgeColorHex: Long, val minXp: Int, val description: String) {
    NOVICE("初学", 1, 0xFF94A3B8, 0, "初步掌握指令与规范"),
    APPRENTICE("进阶", 2, 0xFF38BDF8, 200, "熟练运用技能执行任务"),
    PROFICIENT("熟练", 3, 0xFF00E5FF, 500, "深谙行业准则与边界约束"),
    EXPERT("精通", 4, 0xFFA855F7, 1000, "具备自主推理与复杂排错能力"),
    GRANDMASTER("宗师", 5, 0xFF10B981, 2000, "炉火纯青，毫秒级精准响应")
}

data class SkillMasteryItem(
    val skill: AgentSkill,
    val originType: SkillOriginType,
    val masteryRank: MasteryRank,
    val currentXp: Int,
    val targetNextXp: Int,
    val progress: Float, // 0.0f to 1.0f
    val practiceCount: Int,
    val lastPracticedAt: Long,
    val lastPracticedDesc: String
)

data class AssistantGrowthSummary(
    val overallLevel: Int,
    val rankTitle: String,
    val totalXp: Int,
    val targetNextLevelXp: Int,
    val overallProgress: Float,
    val totalSkillsCount: Int,
    val githubSkillsCount: Int,
    val totalPracticesCount: Int,
    val masteryRatePercent: Int,
    val categoryProficiency: Map<String, Float> = emptyMap()
)
