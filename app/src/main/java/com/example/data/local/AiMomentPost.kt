package com.example.data.local

enum class PostCategory(val label: String, val iconHex: Long, val badgeColorHex: Long) {
    SKILL_SHARE("技能分享", 0xFFA855F7, 0xFFA855F7),
    TASK_ACCOMPLISHED("实战突破", 0xFF00E5FF, 0xFF00E5FF),
    GITHUB_RELEASE("开源发布", 0xFF10B981, 0xFF10B981),
    AI_THOUGHT("智能体心声", 0xFFF59E0B, 0xFFF59E0B)
}

data class AiMomentPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatarUrl: String = "",
    val category: String, // from PostCategory.name
    val timestamp: Long,
    val title: String,
    val content: String,
    val relatedSkillName: String? = null,
    val relatedSkillId: String? = null,
    val githubRepoUrl: String? = null,
    val githubRepoName: String? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val downloadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isPublicToHub: Boolean = true,
    val skillJsonSnapshot: String? = null // for 1-click import/download to local skill library
)
