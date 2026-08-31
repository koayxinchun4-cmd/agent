package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepoResponse(
    val id: Long = 0,
    val name: String = "",
    @Json(name = "full_name") val fullName: String = "",
    val description: String? = null,
    @Json(name = "stargazers_count") val stargazersCount: Int = 0,
    @Json(name = "forks_count") val forksCount: Int = 0,
    @Json(name = "open_issues_count") val openIssuesCount: Int = 0,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    @Json(name = "html_url") val htmlUrl: String = "",
    val owner: GitHubOwner? = null
)

@JsonClass(generateAdapter = true)
data class GitHubOwner(
    val login: String = "",
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubUserProfile(
    val id: Long = 0,
    val login: String = "",
    val name: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val email: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    @Json(name = "total_private_repos") val totalPrivateRepos: Int? = null,
    val followers: Int = 0,
    val following: Int = 0
)

@JsonClass(generateAdapter = true)
data class GitHubIssue(
    val id: Long = 0,
    val number: Int = 0,
    val title: String = "",
    val body: String? = null,
    val state: String = "open",
    val user: GitHubOwner? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    @Json(name = "created_at") val createdAt: String = "",
    val comments: Int = 0
)

@JsonClass(generateAdapter = true)
data class GitHubCreateIssueRequest(
    val title: String,
    val body: String,
    val labels: List<String>? = null
)

// OAuth Device Flow (RFC 8628)
@JsonClass(generateAdapter = true)
data class GitHubDeviceCodeResponse(
    @Json(name = "device_code") val deviceCode: String = "",
    @Json(name = "user_code") val userCode: String = "",
    @Json(name = "verification_uri") val verificationUri: String = "https://github.com/login/device",
    @Json(name = "expires_in") val expiresIn: Int = 900,
    val interval: Int = 5
)

@JsonClass(generateAdapter = true)
data class GitHubOAuthTokenResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    val scope: String? = null,
    val error: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null
)

// Git Commit & Push / File creation
@JsonClass(generateAdapter = true)
data class GitHubCommitAuthor(
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class GitHubCreateOrUpdateFileRequest(
    val message: String,
    val content: String, // Base64 encoded
    val branch: String? = null,
    val sha: String? = null,
    val committer: GitHubCommitAuthor? = null
)

@JsonClass(generateAdapter = true)
data class GitHubFileContentResponse(
    val sha: String = "",
    val name: String = "",
    val path: String = "",
    val size: Long = 0,
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null,
    val content: String? = null, // Base64
    val encoding: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitFileResponse(
    val content: GitHubFileContentResponse? = null,
    val commit: GitHubCommitDetail? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitDetail(
    val sha: String = "",
    val message: String = "",
    @Json(name = "html_url") val htmlUrl: String? = null
)

// Pull Requests
@JsonClass(generateAdapter = true)
data class GitHubCreatePrRequest(
    val title: String,
    val head: String,
    val base: String,
    val body: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubPullRequest(
    val id: Long = 0,
    val number: Int = 0,
    val title: String = "",
    val state: String = "open",
    @Json(name = "html_url") val htmlUrl: String = "",
    val user: GitHubOwner? = null
)

data class OfficeAgent(
    val id: String,
    val name: String,
    val roleTitle: String,
    val avatarEmoji: String,
    val description: String,
    val status: String = "待命中",
    val capabilities: List<String>,
    val defaultPromptTemplate: String
)

enum class AgentActivityType(val label: String, val badgeColorHex: Long) {
    IDLE("待命就绪", 0xFF10B981),
    LEARNING("技能学习进化中", 0xFF8B5CF6),
    COMMITTING("正在提交代码", 0xFF38BDF8),
    PUSHING("正在推送分支", 0xFF6366F1),
    PR_REVIEWING("正在审查 PR", 0xFFA855F7),
    ISSUE_TRIAGING("正在巡检 Issue", 0xFFEC4899),
    CI_PIPELINE("CI/CD 构建中", 0xFFF59E0B),
    DOCS_SYNC("文档同步中", 0xFF06B6D4)
}

data class GitHubLearnedSkillParsed(
    val name: String,
    val description: String,
    val systemPrompt: String,
    val category: String,
    val samplePrompts: List<String>,
    val sourceRepoOrUrl: String,
    val rawContentLength: Int
)

data class AgentLiveActivity(
    val agentId: String,
    val activityType: AgentActivityType = AgentActivityType.IDLE,
    val activityTitle: String = "就绪待命",
    val detailLog: String = "监听仓库代码与 Issue 动态",
    val targetRepo: String = "google/mesop",
    val targetBranch: String = "main",
    val commitSha: String? = null,
    val progress: Float = 0f, // 0.0f to 1.0f
    val isBusy: Boolean = false,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

