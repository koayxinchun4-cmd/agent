package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AgentActivityType
import com.example.data.api.AgentLiveActivity
import com.example.data.api.GitHubDeviceCodeResponse
import com.example.data.api.GitHubIssue
import com.example.data.api.GitHubRepoResponse
import com.example.data.api.GitHubUserProfile
import com.example.data.api.OfficeAgent
import com.example.data.local.AgentMemory
import com.example.data.local.AgentSkill
import com.example.data.local.AssistantGrowthSummary
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.local.AiMomentPost
import com.example.data.local.MasteryRank
import com.example.data.local.SkillMasteryItem
import com.example.data.local.SkillOriginType
import com.example.data.repository.AgentRepository
import com.example.service.TaskCompletionEvent
import com.example.service.TaskCompletionObserver
import com.example.service.TaskCompletedEventType
import com.example.utils.IntentHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgentViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = AgentRepository(application)

    val sessions: StateFlow<List<ChatSession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val skills: StateFlow<List<AgentSkill>> = repository.getAllSkills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _masteryRefreshTrigger = MutableStateFlow(0L)

    val skillsMasteryList: StateFlow<List<SkillMasteryItem>> = combine(
        skills,
        _masteryRefreshTrigger
    ) { currentSkills, _ ->
        calculateMasteryList(currentSkills)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val growthSummary: StateFlow<AssistantGrowthSummary> = skillsMasteryList.map { masteryItems ->
        calculateGrowthSummary(masteryItems)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        AssistantGrowthSummary(
            overallLevel = 1,
            rankTitle = "新星学习型智能体",
            totalXp = 0,
            targetNextLevelXp = 800,
            overallProgress = 0f,
            totalSkillsCount = 0,
            githubSkillsCount = 0,
            totalPracticesCount = 0,
            masteryRatePercent = 0
        )
    )

    val memories: StateFlow<List<AgentMemory>> = repository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _activeSkillId = MutableStateFlow<String?>("general")
    val activeSkillId: StateFlow<String?> = _activeSkillId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Real-time Speech Recognition
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recognizedSpeechText = MutableStateFlow<String?>(null)
    val recognizedSpeechText: StateFlow<String?> = _recognizedSpeechText.asStateFlow()

    private val _partialSpeechText = MutableStateFlow<String>("")
    val partialSpeechText: StateFlow<String> = _partialSpeechText.asStateFlow()

    private val _speechRms = MutableStateFlow(0f)
    val speechRms: StateFlow<Float> = _speechRms.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _apiKey = MutableStateFlow(repository.getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(repository.getSelectedModel())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _lastExportedZip = MutableStateFlow<File?>(null)
    val lastExportedZip: StateFlow<File?> = _lastExportedZip.asStateFlow()

    // Office Agents & GitHub Integration
    val officeAgents: List<OfficeAgent> = repository.getOfficeAgentsList()

    private val _githubRepo = MutableStateFlow(repository.getDefaultGitHubRepo())
    val githubRepo: StateFlow<String> = _githubRepo.asStateFlow()

    private val _githubToken = MutableStateFlow(repository.getGitHubToken())
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _githubClientId = MutableStateFlow(repository.getGitHubClientId().ifBlank { AgentRepository.DEFAULT_GITHUB_CLIENT_ID })
    val githubClientId: StateFlow<String> = _githubClientId.asStateFlow()

    private val _githubClientSecret = MutableStateFlow(repository.getGitHubClientSecret())
    val githubClientSecret: StateFlow<String> = _githubClientSecret.asStateFlow()

    private val _githubUserProfile = MutableStateFlow<GitHubUserProfile?>(null)
    val githubUserProfile: StateFlow<GitHubUserProfile?> = _githubUserProfile.asStateFlow()

    private val _userRepositories = MutableStateFlow<List<GitHubRepoResponse>>(emptyList())
    val userRepositories: StateFlow<List<GitHubRepoResponse>> = _userRepositories.asStateFlow()

    private val _githubRepoDetails = MutableStateFlow<GitHubRepoResponse?>(null)
    val githubRepoDetails: StateFlow<GitHubRepoResponse?> = _githubRepoDetails.asStateFlow()

    private val _githubIssues = MutableStateFlow<List<GitHubIssue>>(emptyList())
    val githubIssues: StateFlow<List<GitHubIssue>> = _githubIssues.asStateFlow()

    private val _isGitHubLoading = MutableStateFlow(false)
    val isGitHubLoading: StateFlow<Boolean> = _isGitHubLoading.asStateFlow()

    private val _gitHubStatusMessage = MutableStateFlow<String?>(null)
    val gitHubStatusMessage: StateFlow<String?> = _gitHubStatusMessage.asStateFlow()

    // GitHub OAuth Device Flow state
    private val _deviceCodeResponse = MutableStateFlow<GitHubDeviceCodeResponse?>(null)
    val deviceCodeResponse: StateFlow<GitHubDeviceCodeResponse?> = _deviceCodeResponse.asStateFlow()

    private val _isOAuthPolling = MutableStateFlow(false)
    val isOAuthPolling: StateFlow<Boolean> = _isOAuthPolling.asStateFlow()

    // Real-time Agent Live Activities on GitHub
    private val _agentLiveActivities = MutableStateFlow<Map<String, AgentLiveActivity>>(emptyMap())
    val agentLiveActivities: StateFlow<Map<String, AgentLiveActivity>> = _agentLiveActivities.asStateFlow()

    private val _isLearningSkill = MutableStateFlow(false)
    val isLearningSkill: StateFlow<Boolean> = _isLearningSkill.asStateFlow()

    private val _learningSkillStatus = MutableStateFlow<String?>(null)
    val learningSkillStatus: StateFlow<String?> = _learningSkillStatus.asStateFlow()

    // AI Moments state
    private val _aiMoments = MutableStateFlow<List<AiMomentPost>>(repository.loadAiMoments())
    val aiMoments: StateFlow<List<AiMomentPost>> = _aiMoments.asStateFlow()

    private val _recentTaskNotification = MutableStateFlow<String?>(null)
    val recentTaskNotification: StateFlow<String?> = _recentTaskNotification.asStateFlow()

    private var oauthPollingJob: Job? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(application, this)
        initAgentLiveActivities()
        loadInitialSession()
        refreshGitHubRepoInfo(_githubRepo.value)
        if (_githubToken.value.isNotBlank()) {
            refreshGitHubUserProfile()
        }
        setupTaskCompletionObserver()
    }

    private fun setupTaskCompletionObserver() {
        viewModelScope.launch {
            TaskCompletionObserver.eventsFlow.collect { event ->
                handleTaskCompletionEvent(event)
            }
        }
    }

    /**
     * Background observer callback handler that automatically increments skill progress,
     * updates XP mastery, and logs accomplishments
     */
    private fun handleTaskCompletionEvent(event: TaskCompletionEvent) {
        val skillId = event.relatedSkillId ?: _activeSkillId.value ?: "general"
        // 1. Increment skill practice count & XP
        repository.incrementSkillPractice(skillId, event.xpGain)
        _masteryRefreshTrigger.value = System.currentTimeMillis()

        // 2. Format notification for user toast / HUD
        _recentTaskNotification.value = "🎯 完成任务: ${event.taskTitle} (技能已成长 +${event.xpGain} XP)"

        // 3. Automatically record memory of task accomplishment
        viewModelScope.launch {
            repository.addMemory(
                key = "实战成就: ${event.taskTitle}",
                content = "于 ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date())} 完成任务【${event.taskTitle}】(${event.detailSummary})，熟练度提升 +${event.xpGain} XP",
                category = "task"
            )
        }
    }

    fun clearTaskNotification() {
        _recentTaskNotification.value = null
    }

    private fun initAgentLiveActivities() {
        val repo = _githubRepo.value
        val initialMap = mapOf(
            "agent_codex_dev" to AgentLiveActivity(
                agentId = "agent_codex_dev",
                activityType = AgentActivityType.IDLE,
                activityTitle = "代码就绪",
                detailLog = "监听 main 分支代码变动，随时可提交补丁",
                targetRepo = repo,
                targetBranch = "main"
            ),
            "agent_devops_ci" to AgentLiveActivity(
                agentId = "agent_devops_ci",
                activityType = AgentActivityType.IDLE,
                activityTitle = "CI/CD 待命",
                detailLog = "监控 GitHub Actions 与自动化工作流",
                targetRepo = repo,
                targetBranch = "main"
            ),
            "agent_issue_triage" to AgentLiveActivity(
                agentId = "agent_issue_triage",
                activityType = AgentActivityType.IDLE,
                activityTitle = "巡检就绪",
                detailLog = "监听开放 Issues 与 Bug 报告",
                targetRepo = repo,
                targetBranch = "main"
            ),
            "agent_docs_writer" to AgentLiveActivity(
                agentId = "agent_docs_writer",
                activityType = AgentActivityType.IDLE,
                activityTitle = "文档待命",
                detailLog = "准备同步 README、API 与架构文档",
                targetRepo = repo,
                targetBranch = "main"
            ),
            "agent_scrum_pm" to AgentLiveActivity(
                agentId = "agent_scrum_pm",
                activityType = AgentActivityType.IDLE,
                activityTitle = "看板待命",
                detailLog = "跟踪 PR 状态与 Milestone 进度",
                targetRepo = repo,
                targetBranch = "main"
            )
        )
        _agentLiveActivities.value = initialMap
    }

    fun updateAgentLiveActivity(agentId: String, activity: AgentLiveActivity) {
        val current = _agentLiveActivities.value.toMutableMap()
        current[agentId] = activity
        _agentLiveActivities.value = current
    }

    private fun loadInitialSession() {
        viewModelScope.launch {
            repository.getAllSessions().collect { sessionList ->
                if (_currentSessionId.value == null) {
                    if (sessionList.isNotEmpty()) {
                        selectSession(sessionList.first().id)
                    } else {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            val session = repository.getSession(sessionId)
            if (session?.activeSkillId != null) {
                _activeSkillId.value = session.activeSkillId
            }
            repository.getMessagesForSession(sessionId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun createNewSession(skillId: String? = _activeSkillId.value, customTitle: String? = null) {
        viewModelScope.launch {
            val newSession = repository.createNewSession(skillId = skillId, title = customTitle)
            selectSession(newSession.id)
        }
    }

    fun deleteSession(session: ChatSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            if (_currentSessionId.value == session.id) {
                _currentSessionId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun setActiveSkill(skillId: String?) {
        _activeSkillId.value = skillId
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isLoading.value) return

        val sessionId = _currentSessionId.value ?: return

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = repository.sendMessage(
                sessionId = sessionId,
                userPrompt = trimmed,
                activeSkillId = _activeSkillId.value
            )

            _isLoading.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            } else {
                // Task succeeded, trigger task observer to boost progress
                val activeSkill = skills.value.firstOrNull { it.id == _activeSkillId.value }
                val eventType = when (_activeSkillId.value) {
                    "codex" -> TaskCompletedEventType.CODE_RUN
                    "office" -> TaskCompletedEventType.DOCUMENT_EDIT
                    "automation" -> TaskCompletedEventType.SYSTEM_AUTOMATION_EXEC
                    else -> TaskCompletedEventType.CHAT_MESSAGE_RESOLVED
                }
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = eventType,
                    taskTitle = trimmed.take(24) + if (trimmed.length > 24) "..." else "",
                    detailSummary = "技能【${activeSkill?.name ?: "通用"}】实战推理完成",
                    relatedSkillId = _activeSkillId.value,
                    targetRepo = _githubRepo.value,
                    xpGain = 30
                )
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveApiKey(key: String) {
        repository.setCustomApiKey(key)
        _apiKey.value = repository.getApiKey()
    }

    fun setModel(model: String) {
        repository.setSelectedModel(model)
        _selectedModel.value = model
    }

    fun createCustomSkill(
        name: String,
        description: String,
        systemPrompt: String,
        category: String,
        samplePrompts: List<String>
    ) {
        viewModelScope.launch {
            val id = "custom_" + System.currentTimeMillis()
            val promptsJson = "[" + samplePrompts.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" } + "]"
            val skill = AgentSkill(
                id = id,
                name = name,
                description = description,
                iconName = "psychology",
                category = category,
                systemPrompt = systemPrompt,
                samplePromptsJson = promptsJson,
                isBuiltIn = false
            )
            repository.saveCustomSkill(skill)
            _activeSkillId.value = id
        }
    }

    /**
     * 一键从 GitHub 学习/导入技能：获取 SKILL.md/文档 -> AI 提炼系统指令与示例 -> 持久化到技能库并激活
     */
    fun learnSkillFromGitHub(repoOrUrl: String, onComplete: (Boolean, String) -> Unit) {
        val input = repoOrUrl.trim()
        if (input.isBlank()) {
            onComplete(false, "请输入 GitHub 仓库路径或文件链接")
            return
        }

        _isLearningSkill.value = true
        _learningSkillStatus.value = "正在从 GitHub 获取技能文档与 SKILL.md..."

        // Update Codex Agent activity to LEARNING
        updateAgentLiveActivity(
            "agent_codex_dev",
            AgentLiveActivity(
                agentId = "agent_codex_dev",
                activityType = AgentActivityType.LEARNING,
                activityTitle = "正在学习 GitHub 技能",
                detailLog = "提取 $input 的规范与技能定义",
                targetRepo = _githubRepo.value,
                targetBranch = "main",
                progress = 0.3f,
                isBusy = true
            )
        )

        viewModelScope.launch {
            val result = repository.fetchAndLearnGitHubSkill(input)
            _isLearningSkill.value = false

            if (result.isSuccess) {
                val parsed = result.getOrNull()!!
                val newSkillId = "gh_skill_" + System.currentTimeMillis()
                val promptsJson = "[" + parsed.samplePrompts.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" } + "]"

                val icon = when (parsed.category.lowercase()) {
                    "coding" -> "code"
                    "office" -> "description"
                    "automation" -> "bolt"
                    else -> "psychology"
                }

                val newSkill = AgentSkill(
                    id = newSkillId,
                    name = parsed.name,
                    description = parsed.description,
                    iconName = icon,
                    category = parsed.category,
                    systemPrompt = parsed.systemPrompt,
                    samplePromptsJson = promptsJson,
                    isBuiltIn = false
                )

                repository.saveCustomSkill(newSkill)
                _activeSkillId.value = newSkillId
                _learningSkillStatus.value = "🎉 成功习得新技能：${parsed.name}！"

                // Update Codex Agent activity back to ready
                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "已掌握 ${parsed.name}",
                        detailLog = "已深度学习并激活技能：${parsed.name}",
                        targetRepo = _githubRepo.value,
                        targetBranch = "main",
                        progress = 1.0f,
                        isBusy = false
                    )
                )

                // Also create memory of learned skill
                repository.addMemory(
                    key = "GitHub 学习技能: ${parsed.name}",
                    content = "于 GitHub 导入掌握技能【${parsed.name}】(${parsed.sourceRepoOrUrl})，能力范围：${parsed.description}",
                    category = "learning"
                )

                onComplete(true, "🎉 成功从 GitHub 深度学习并激活【${parsed.name}】！")
            } else {
                val err = result.exceptionOrNull()?.message ?: "从 GitHub 学习技能失败"
                _learningSkillStatus.value = "学习失败: $err"

                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "技能学习失败",
                        detailLog = err,
                        targetRepo = _githubRepo.value,
                        targetBranch = "main",
                        isBusy = false
                    )
                )

                onComplete(false, err)
            }
        }
    }

    fun deleteSkill(skill: AgentSkill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
            if (_activeSkillId.value == skill.id) {
                _activeSkillId.value = "general"
            }
            _masteryRefreshTrigger.value = System.currentTimeMillis()
        }
    }

    /**
     * 针对指定技能进行强化特训，增加经验值与熟练度
     */
    fun boostSkillTraining(skillId: String, onDone: (Int) -> Unit = {}) {
        val newXp = repository.boostSkillTraining(skillId, 60)
        _masteryRefreshTrigger.value = System.currentTimeMillis()
        onDone(newXp)
    }

    private fun calculateMasteryList(skillList: List<AgentSkill>): List<SkillMasteryItem> {
        val now = System.currentTimeMillis()
        return skillList.map { skill ->
            val origin = when {
                skill.id.startsWith("gh_skill_") -> SkillOriginType.GITHUB_LEARNED
                skill.isBuiltIn -> SkillOriginType.BUILT_IN
                else -> SkillOriginType.CUSTOM
            }

            val baseOriginXp = when (origin) {
                SkillOriginType.BUILT_IN -> 600
                SkillOriginType.GITHUB_LEARNED -> 450
                SkillOriginType.CUSTOM -> 200
            }

            val extraXp = repository.getSkillExtraXp(skill.id)
            val totalXp = baseOriginXp + extraXp
            val practiceCount = repository.getSkillPracticeCount(skill.id)
            val lastPracticedAt = repository.getSkillLastPracticeTime(skill.id)

            val rank: MasteryRank
            val targetNextXp: Int
            val progress: Float

            when {
                totalXp >= MasteryRank.GRANDMASTER.minXp -> {
                    rank = MasteryRank.GRANDMASTER
                    targetNextXp = 3000
                    progress = ((totalXp - 2000).toFloat() / 1000f).coerceIn(0.1f, 1f)
                }
                totalXp >= MasteryRank.EXPERT.minXp -> {
                    rank = MasteryRank.EXPERT
                    targetNextXp = MasteryRank.GRANDMASTER.minXp
                    progress = ((totalXp - 1000).toFloat() / 1000f).coerceIn(0.05f, 1f)
                }
                totalXp >= MasteryRank.PROFICIENT.minXp -> {
                    rank = MasteryRank.PROFICIENT
                    targetNextXp = MasteryRank.EXPERT.minXp
                    progress = ((totalXp - 500).toFloat() / 500f).coerceIn(0.05f, 1f)
                }
                totalXp >= MasteryRank.APPRENTICE.minXp -> {
                    rank = MasteryRank.APPRENTICE
                    targetNextXp = MasteryRank.PROFICIENT.minXp
                    progress = ((totalXp - 200).toFloat() / 300f).coerceIn(0.05f, 1f)
                }
                else -> {
                    rank = MasteryRank.NOVICE
                    targetNextXp = MasteryRank.APPRENTICE.minXp
                    progress = (totalXp.toFloat() / 200f).coerceIn(0.05f, 1f)
                }
            }

            val lastDesc = if (lastPracticedAt == 0L) {
                if (origin == SkillOriginType.BUILT_IN) "原生掌握" else "刚解析掌握"
            } else {
                val diffSec = (now - lastPracticedAt) / 1000
                when {
                    diffSec < 60 -> "刚刚完成实战"
                    diffSec < 3600 -> "${diffSec / 60} 分钟前"
                    diffSec < 86400 -> "${diffSec / 3600} 小时前"
                    else -> "${diffSec / 86400} 天前"
                }
            }

            SkillMasteryItem(
                skill = skill,
                originType = origin,
                masteryRank = rank,
                currentXp = totalXp,
                targetNextXp = targetNextXp,
                progress = progress,
                practiceCount = practiceCount,
                lastPracticedAt = lastPracticedAt,
                lastPracticedDesc = lastDesc
            )
        }.sortedWith(compareByDescending<SkillMasteryItem> { it.masteryRank.levelNumber }.thenByDescending { it.currentXp })
    }

    private fun calculateGrowthSummary(items: List<SkillMasteryItem>): AssistantGrowthSummary {
        if (items.isEmpty()) {
            return AssistantGrowthSummary(
                overallLevel = 1,
                rankTitle = "新星学习型智能体",
                totalXp = 0,
                targetNextLevelXp = 800,
                overallProgress = 0f,
                totalSkillsCount = 0,
                githubSkillsCount = 0,
                totalPracticesCount = 0,
                masteryRatePercent = 0
            )
        }

        val totalXp = items.sumOf { it.currentXp }
        val overallLevel = 1 + (totalXp / 800)
        val currentLevelBaseXp = (overallLevel - 1) * 800
        val nextLevelTargetXp = overallLevel * 800
        val overallProgress = ((totalXp - currentLevelBaseXp).toFloat() / 800f).coerceIn(0.05f, 1.0f)

        val rankTitle = when {
            overallLevel >= 10 -> "全域大宗师级 AI 智能体"
            overallLevel >= 7 -> "资深多模态架构师"
            overallLevel >= 5 -> "高阶全能工程师"
            overallLevel >= 3 -> "进阶业务专家"
            else -> "新星学习型智能体"
        }

        val ghCount = items.count { it.originType == SkillOriginType.GITHUB_LEARNED }
        val totalPractices = items.sumOf { it.practiceCount }

        val avgMasteryPercent = if (items.isNotEmpty()) {
            (items.map { it.progress }.average() * 100).toInt().coerceIn(10, 99)
        } else 0

        val categoryMap = mutableMapOf<String, Float>()
        items.groupBy { it.skill.category }.forEach { (cat, catItems) ->
            categoryMap[cat] = catItems.map { it.progress }.average().toFloat().coerceIn(0.1f, 1f)
        }

        return AssistantGrowthSummary(
            overallLevel = overallLevel,
            rankTitle = rankTitle,
            totalXp = totalXp,
            targetNextLevelXp = nextLevelTargetXp,
            overallProgress = overallProgress,
            totalSkillsCount = items.size,
            githubSkillsCount = ghCount,
            totalPracticesCount = totalPractices,
            masteryRatePercent = avgMasteryPercent,
            categoryProficiency = categoryMap
        )
    }

    fun addMemory(key: String, content: String, category: String = "custom") {
        viewModelScope.launch {
            repository.addMemory(key, content, category)
        }
    }

    fun deleteMemory(memory: AgentMemory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun exportCurrentChatToZip(onReady: (File) -> Unit) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            val zipFile = repository.exportChatToZip(sessionId)
            _lastExportedZip.value = zipFile
            onReady(zipFile)
        }
    }

    // TTS implementation
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            ttsReady = true
        }
    }

    fun speakText(text: String) {
        if (!ttsReady) return
        val cleanText = text.replace("`", "").replace("#", "").replace("*", "").take(800)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "AgentTTS")
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    // SpeechRecognizer Native API Integration
    fun startListening() {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "当前系统未找到可用的语音识别服务组件"
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isRecording.value = true
                        _partialSpeechText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _isRecording.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _speechRms.value = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isRecording.value = false
                        _speechRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isRecording.value = false
                        _speechRms.value = 0f
                        val errorText = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "音频录制异常"
                            SpeechRecognizer.ERROR_CLIENT -> "语音客户端异常"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "缺少录音权限"
                            SpeechRecognizer.ERROR_NETWORK -> "网络连接异常"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络连接超时"
                            SpeechRecognizer.ERROR_NO_MATCH -> "未清晰识别到语音"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "语音引擎正忙，请重试"
                            SpeechRecognizer.ERROR_SERVER -> "语音识别服务器异常"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到说话声"
                            else -> "语音识别错误: $error"
                        }
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            _errorMessage.value = errorText
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            _recognizedSpeechText.value = recognized
                            _partialSpeechText.value = recognized
                        }
                        _isRecording.value = false
                        _speechRms.value = 0f
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!partials.isNullOrEmpty()) {
                            val text = partials[0]
                            _partialSpeechText.value = text
                            _recognizedSpeechText.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
            _isRecording.value = true
        } catch (e: Exception) {
            _isRecording.value = false
            _errorMessage.value = "启动语音识别失败: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _isRecording.value = false
        _speechRms.value = 0f
    }

    fun clearRecognizedText() {
        _recognizedSpeechText.value = null
        _partialSpeechText.value = ""
    }

    // GitHub OAuth & Account Operations
    fun saveGitHubToken(token: String) {
        repository.setGitHubToken(token)
        _githubToken.value = token.trim()
        if (token.isNotBlank()) {
            refreshGitHubUserProfile()
        } else {
            _githubUserProfile.value = null
        }
    }

    fun unbindGitHubAccount() {
        cancelOAuthDeviceFlow()
        repository.clearGitHubToken()
        _githubToken.value = ""
        _githubUserProfile.value = null
        _gitHubStatusMessage.value = "已解除 GitHub 账号绑定"
    }

    fun saveGitHubClientId(clientId: String) {
        repository.setGitHubClientId(clientId)
        _githubClientId.value = clientId.trim()
    }

    fun saveGitHubClientSecret(secret: String) {
        repository.setGitHubClientSecret(secret)
        _githubClientSecret.value = secret.trim()
    }

    fun refreshGitHubUserProfile() {
        if (_githubToken.value.isBlank()) return
        viewModelScope.launch {
            val result = repository.fetchCurrentUserProfile()
            if (result.isSuccess) {
                val user = result.getOrNull()
                _githubUserProfile.value = user
                _gitHubStatusMessage.value = "GitHub 账号已绑定: @${user?.login}"
                refreshUserRepositories()
            } else {
                _gitHubStatusMessage.value = "GitHub Token 验证失效: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun refreshUserRepositories() {
        if (_githubToken.value.isBlank()) return
        viewModelScope.launch {
            val result = repository.fetchUserRepositories()
            if (result.isSuccess) {
                _userRepositories.value = result.getOrDefault(emptyList())
            }
        }
    }

    fun openGitHubOAuthInBrowser(context: Context, customClientId: String? = null) {
        val authUrl = repository.getOAuthAuthorizeUrl(customClientId)
        IntentHelper.openBrowserUrl(context, authUrl)
    }

    fun startGitHubDeviceFlow(customClientId: String? = null) {
        val clientId = customClientId?.ifBlank { null } ?: _githubClientId.value.ifBlank { AgentRepository.DEFAULT_GITHUB_CLIENT_ID }
        _isOAuthPolling.value = true
        _gitHubStatusMessage.value = "正在向 GitHub 请求设备验证码..."

        viewModelScope.launch {
            val result = repository.requestDeviceCode(clientId)
            if (result.isSuccess) {
                val data = result.getOrNull()!!
                _deviceCodeResponse.value = data
                _gitHubStatusMessage.value = "请在浏览器打开验证页面并输入授权码: ${data.userCode}"

                // Start polling until token is received or expired
                startDeviceTokenPolling(clientId, data.deviceCode, data.interval, data.expiresIn)
            } else {
                _isOAuthPolling.value = false
                _gitHubStatusMessage.value = result.exceptionOrNull()?.message ?: "请求 GitHub 设备码失败"
            }
        }
    }

    private fun startDeviceTokenPolling(clientId: String, deviceCode: String, intervalSeconds: Int, expiresInSeconds: Int) {
        oauthPollingJob?.cancel()
        oauthPollingJob = viewModelScope.launch {
            val delayMs = ((if (intervalSeconds < 5) 5 else intervalSeconds) * 1000L)
            val startTime = System.currentTimeMillis()
            val maxTimeMs = expiresInSeconds * 1000L

            while (System.currentTimeMillis() - startTime < maxTimeMs) {
                delay(delayMs)
                val pollResult = repository.pollDeviceToken(clientId, deviceCode)
                if (pollResult.isSuccess) {
                    val tokenResp = pollResult.getOrNull()
                    if (!tokenResp?.accessToken.isNullOrBlank()) {
                        _githubToken.value = tokenResp!!.accessToken!!
                        _deviceCodeResponse.value = null
                        _isOAuthPolling.value = false
                        _gitHubStatusMessage.value = "🎉 GitHub 账号授权成功！"
                        refreshGitHubUserProfile()
                        refreshGitHubRepoInfo(_githubRepo.value)
                        break
                    } else if (tokenResp?.error == "authorization_pending") {
                        // User hasn't completed flow in browser yet, continue polling
                        _gitHubStatusMessage.value = "等待用户在 GitHub 页面批准授权..."
                    } else if (tokenResp?.error == "slow_down") {
                        delay(5000L)
                    } else if (tokenResp?.error == "expired_token") {
                        _isOAuthPolling.value = false
                        _deviceCodeResponse.value = null
                        _gitHubStatusMessage.value = "授权码已过期，请重新发起绑定"
                        break
                    } else if (tokenResp?.error == "access_denied") {
                        _isOAuthPolling.value = false
                        _deviceCodeResponse.value = null
                        _gitHubStatusMessage.value = "用户已拒绝授权"
                        break
                    }
                }
            }
            _isOAuthPolling.value = false
        }
    }

    fun cancelOAuthDeviceFlow() {
        oauthPollingJob?.cancel()
        oauthPollingJob = null
        _deviceCodeResponse.value = null
        _isOAuthPolling.value = false
    }

    fun handleOAuthDeepLink(uri: Uri) {
        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            val clientId = _githubClientId.value.ifBlank { AgentRepository.DEFAULT_GITHUB_CLIENT_ID }
            val clientSecret = _githubClientSecret.value
            if (clientSecret.isNotBlank()) {
                viewModelScope.launch {
                    _isGitHubLoading.value = true
                    val result = repository.exchangeOAuthWebCode(clientId, clientSecret, code)
                    _isGitHubLoading.value = false
                    if (result.isSuccess) {
                        _githubToken.value = result.getOrNull() ?: ""
                        _gitHubStatusMessage.value = "🎉 GitHub OAuth 授权成功！"
                        refreshGitHubUserProfile()
                        refreshGitHubRepoInfo(_githubRepo.value)
                    } else {
                        _gitHubStatusMessage.value = "OAuth Token 换取失败: ${result.exceptionOrNull()?.message}"
                    }
                }
            } else {
                _gitHubStatusMessage.value = "接收到 OAuth Code，但未配置 GitHub Client Secret"
            }
        }
    }

    fun saveGitHubRepo(repo: String) {
        val clean = repo.trim().removePrefix("https://github.com/").removeSuffix(".git")
        repository.setDefaultGitHubRepo(clean)
        _githubRepo.value = clean
        refreshGitHubRepoInfo(clean)
    }

    fun refreshGitHubRepoInfo(repoInput: String) {
        val parts = repoInput.split("/")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return

        val owner = parts[0].trim()
        val repo = parts[1].trim()

        _isGitHubLoading.value = true
        _gitHubStatusMessage.value = null

        viewModelScope.launch {
            val repoResult = repository.fetchGitHubRepo(owner, repo)
            if (repoResult.isSuccess) {
                _githubRepoDetails.value = repoResult.getOrNull()
                _gitHubStatusMessage.value = "已连接仓库: $owner/$repo"
            } else {
                _gitHubStatusMessage.value = "无法获取仓库信息: ${repoResult.exceptionOrNull()?.message}"
            }

            val issuesResult = repository.fetchGitHubIssues(owner, repo)
            if (issuesResult.isSuccess) {
                _githubIssues.value = issuesResult.getOrDefault(emptyList())
            }

            _isGitHubLoading.value = false
        }
    }

    fun createIssueOnGitHub(title: String, body: String, labels: List<String> = emptyList(), onResult: (Boolean, String) -> Unit) {
        val parts = _githubRepo.value.split("/")
        if (parts.size != 2) {
            onResult(false, "仓库名称格式错误，应为 owner/repo")
            return
        }

        _isGitHubLoading.value = true
        updateAgentLiveActivity(
            "agent_issue_triage",
            AgentLiveActivity(
                agentId = "agent_issue_triage",
                activityType = AgentActivityType.ISSUE_TRIAGING,
                activityTitle = "正在提交 Issue",
                detailLog = "正在创建并标记: $title",
                targetRepo = _githubRepo.value,
                targetBranch = "issues",
                progress = 0.5f,
                isBusy = true
            )
        )

        viewModelScope.launch {
            val result = repository.createGitHubIssue(parts[0], parts[1], title, body, labels)
            _isGitHubLoading.value = false
            if (result.isSuccess) {
                val issue = result.getOrNull()
                updateAgentLiveActivity(
                    "agent_issue_triage",
                    AgentLiveActivity(
                        agentId = "agent_issue_triage",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "Issue #${issue?.number} 已创建",
                        detailLog = "已完成 Issue 登记: ${issue?.title}",
                        targetRepo = _githubRepo.value,
                        targetBranch = "issues",
                        progress = 1.0f,
                        isBusy = false
                    )
                )
                refreshGitHubRepoInfo(_githubRepo.value)

                // Background Task Observer Notification (+35 XP)
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = TaskCompletedEventType.GITHUB_ISSUE_SUBMIT,
                    taskTitle = "创建 Issue #${issue?.number}: ${issue?.title}",
                    detailSummary = "在 ${_githubRepo.value} 登记 Bug/需求",
                    relatedSkillId = _activeSkillId.value ?: "automation",
                    targetRepo = _githubRepo.value,
                    xpGain = 35
                )

                onResult(true, "成功在 GitHub 创建 Issue #${issue?.number}: ${issue?.title}")
            } else {
                updateAgentLiveActivity(
                    "agent_issue_triage",
                    AgentLiveActivity(
                        agentId = "agent_issue_triage",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "Issue 创建失败",
                        detailLog = result.exceptionOrNull()?.message ?: "未知错误",
                        targetRepo = _githubRepo.value,
                        targetBranch = "issues",
                        isBusy = false
                    )
                )
                onResult(false, result.exceptionOrNull()?.message ?: "创建 Issue 失败")
            }
        }
    }

    fun executeGitCommitAndPush(
        filePath: String,
        content: String,
        commitMessage: String,
        branch: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        val parts = _githubRepo.value.split("/")
        if (parts.size != 2) {
            onResult(false, "仓库格式无效，需为 owner/repo")
            return
        }

        val targetBranch = branch?.ifBlank { null } ?: _githubRepoDetails.value?.defaultBranch ?: "main"
        _isGitHubLoading.value = true

        // Update Codex Agent to Committing & Pushing
        updateAgentLiveActivity(
            "agent_codex_dev",
            AgentLiveActivity(
                agentId = "agent_codex_dev",
                activityType = AgentActivityType.COMMITTING,
                activityTitle = "正在提交代码补丁",
                detailLog = "准备提交 $filePath 至分支 $targetBranch",
                targetRepo = _githubRepo.value,
                targetBranch = targetBranch,
                progress = 0.4f,
                isBusy = true
            )
        )

        viewModelScope.launch {
            // Simulated step transition for live visualization
            delay(400)
            updateAgentLiveActivity(
                "agent_codex_dev",
                AgentLiveActivity(
                    agentId = "agent_codex_dev",
                    activityType = AgentActivityType.PUSHING,
                    activityTitle = "正在推送代码至 GitHub",
                    detailLog = "向 $targetBranch 推送变更: $commitMessage",
                    targetRepo = _githubRepo.value,
                    targetBranch = targetBranch,
                    progress = 0.8f,
                    isBusy = true
                )
            )

            val result = repository.commitAndPushFile(
                owner = parts[0],
                repo = parts[1],
                filePath = filePath,
                content = content,
                commitMessage = commitMessage,
                branch = targetBranch
            )
            _isGitHubLoading.value = false
            if (result.isSuccess) {
                val commitResp = result.getOrNull()
                val shaShort = commitResp?.commit?.sha?.take(7) ?: "success"

                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "Commit $shaShort 已推送",
                        detailLog = "成功推送 $filePath 至 $targetBranch",
                        targetRepo = _githubRepo.value,
                        targetBranch = targetBranch,
                        commitSha = shaShort,
                        progress = 1.0f,
                        isBusy = false
                    )
                )

                // Trigger DevOps Agent CI notification
                updateAgentLiveActivity(
                    "agent_devops_ci",
                    AgentLiveActivity(
                        agentId = "agent_devops_ci",
                        activityType = AgentActivityType.CI_PIPELINE,
                        activityTitle = "CI 流水线触发中",
                        detailLog = "监测到新提交 $shaShort，GitHub Actions 触发",
                        targetRepo = _githubRepo.value,
                        targetBranch = targetBranch,
                        progress = 0.9f,
                        isBusy = false
                    )
                )

                // Background Task Observer Notification (+45 XP)
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = TaskCompletedEventType.GITHUB_COMMIT_PUSH,
                    taskTitle = "推送 Commit $shaShort 至 GitHub",
                    detailSummary = "提交 $filePath ($commitMessage)",
                    relatedSkillId = _activeSkillId.value ?: "codex",
                    targetRepo = _githubRepo.value,
                    xpGain = 45
                )

                onResult(true, "🚀 Git Commit & Push 成功！Commit: $shaShort 分支: $targetBranch")
            } else {
                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "推送失败",
                        detailLog = result.exceptionOrNull()?.message ?: "Git 提交失败",
                        targetRepo = _githubRepo.value,
                        targetBranch = targetBranch,
                        isBusy = false
                    )
                )
                onResult(false, result.exceptionOrNull()?.message ?: "Git Commit & Push 失败")
            }
        }
    }

    fun commitCodeSnippetToGitHub(
        repoFullName: String,
        filePath: String,
        codeContent: String,
        commitMessage: String,
        branch: String = "main",
        onResult: (Boolean, String, String?) -> Unit
    ) {
        val cleanRepo = repoFullName.trim().removePrefix("https://github.com/").removeSuffix(".git")
        val parts = cleanRepo.split("/")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            onResult(false, "仓库名称格式错误，应为 owner/repo (例如 username/my-repo)", null)
            return
        }

        val owner = parts[0].trim()
        val repo = parts[1].trim()
        val targetBranch = branch.ifBlank { "main" }
        val cleanFilePath = filePath.trim().removePrefix("/")
        val finalCommitMsg = commitMessage.ifBlank { "feat(agent): commit generated code by Nexus AI Agent" }

        _isGitHubLoading.value = true
        updateAgentLiveActivity(
            "agent_codex_dev",
            AgentLiveActivity(
                agentId = "agent_codex_dev",
                activityType = AgentActivityType.COMMITTING,
                activityTitle = "正在提交代码至个人仓库",
                detailLog = "准备提交 $cleanFilePath 至 $cleanRepo ($targetBranch)",
                targetRepo = cleanRepo,
                targetBranch = targetBranch,
                progress = 0.5f,
                isBusy = true
            )
        )

        viewModelScope.launch {
            val result = repository.commitAndPushFile(
                owner = owner,
                repo = repo,
                filePath = cleanFilePath,
                content = codeContent,
                commitMessage = finalCommitMsg,
                branch = targetBranch
            )
            _isGitHubLoading.value = false

            if (result.isSuccess) {
                val commitResp = result.getOrNull()
                val commitSha = commitResp?.commit?.sha?.take(7) ?: "success"
                val fileHtmlUrl = commitResp?.content?.htmlUrl
                    ?: "https://github.com/$owner/$repo/blob/$targetBranch/$cleanFilePath"

                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "代码已成功推送至 GitHub",
                        detailLog = "Commit $commitSha: $cleanFilePath",
                        targetRepo = cleanRepo,
                        targetBranch = targetBranch,
                        commitSha = commitSha,
                        progress = 1.0f,
                        isBusy = false
                    )
                )

                // Background Task Observer Notification (+50 XP)
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = TaskCompletedEventType.GITHUB_COMMIT_PUSH,
                    taskTitle = "提交生成代码至 GitHub 仓库 ($cleanRepo)",
                    detailSummary = "文件: $cleanFilePath, 分支: $targetBranch ($finalCommitMsg)",
                    relatedSkillId = _activeSkillId.value ?: "codex",
                    targetRepo = cleanRepo,
                    xpGain = 50
                )

                onResult(true, "🎉 成功提交代码至 GitHub！Commit: $commitSha ($targetBranch)", fileHtmlUrl)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Git 提交失败"
                updateAgentLiveActivity(
                    "agent_codex_dev",
                    AgentLiveActivity(
                        agentId = "agent_codex_dev",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "代码提交失败",
                        detailLog = errorMsg,
                        targetRepo = cleanRepo,
                        targetBranch = targetBranch,
                        isBusy = false
                    )
                )
                onResult(false, errorMsg, null)
            }
        }
    }

    fun createPullRequest(
        title: String,
        headBranch: String,
        baseBranch: String,
        body: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        val parts = _githubRepo.value.split("/")
        if (parts.size != 2) {
            onResult(false, "仓库格式无效")
            return
        }

        _isGitHubLoading.value = true
        updateAgentLiveActivity(
            "agent_scrum_pm",
            AgentLiveActivity(
                agentId = "agent_scrum_pm",
                activityType = AgentActivityType.PR_REVIEWING,
                activityTitle = "正在发起 PR 审查",
                detailLog = "创建 PR: $title ($headBranch -> $baseBranch)",
                targetRepo = _githubRepo.value,
                targetBranch = baseBranch,
                progress = 0.5f,
                isBusy = true
            )
        )

        viewModelScope.launch {
            val result = repository.createPullRequest(
                owner = parts[0],
                repo = parts[1],
                title = title,
                headBranch = headBranch,
                baseBranch = baseBranch,
                body = body
            )
            _isGitHubLoading.value = false
            if (result.isSuccess) {
                val pr = result.getOrNull()
                updateAgentLiveActivity(
                    "agent_scrum_pm",
                    AgentLiveActivity(
                        agentId = "agent_scrum_pm",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "PR #${pr?.number} 已开启",
                        detailLog = "已向 $baseBranch 提交 PR: ${pr?.title}",
                        targetRepo = _githubRepo.value,
                        targetBranch = baseBranch,
                        progress = 1.0f,
                        isBusy = false
                    )
                )

                // Background Task Observer Notification (+50 XP)
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = TaskCompletedEventType.GITHUB_PR_CREATE,
                    taskTitle = "创建 PR #${pr?.number}: ${pr?.title}",
                    detailSummary = "向 $baseBranch 提交 PR",
                    relatedSkillId = _activeSkillId.value ?: "automation",
                    targetRepo = _githubRepo.value,
                    xpGain = 50
                )

                onResult(true, "🎉 成功创建 Pull Request #${pr?.number}: ${pr?.title}")
            } else {
                updateAgentLiveActivity(
                    "agent_scrum_pm",
                    AgentLiveActivity(
                        agentId = "agent_scrum_pm",
                        activityType = AgentActivityType.IDLE,
                        activityTitle = "PR 创建失败",
                        detailLog = result.exceptionOrNull()?.message ?: "失败",
                        targetRepo = _githubRepo.value,
                        targetBranch = baseBranch,
                        isBusy = false
                    )
                )
                onResult(false, result.exceptionOrNull()?.message ?: "创建 PR 失败")
            }
        }
    }

    fun dispatchOfficeAgentTask(agent: OfficeAgent, taskPrompt: String) {
        val repoName = _githubRepo.value
        val repoContext = _githubRepoDetails.value?.let { repo ->
            "\n[目标 GitHub 仓库]: ${repo.fullName}\n- 描述: ${repo.description ?: "无"}\n- 默认分支: ${repo.defaultBranch}\n- 开放 Issues: ${repo.openIssuesCount} 个\n- Stars: ${repo.stargazersCount}\n"
        } ?: "\n[目标 GitHub 仓库]: $repoName\n"

        val authContext = _githubUserProfile.value?.let { user ->
            "\n[当前已绑定 GitHub 身份]: @${user.login} (${user.name ?: "开发者"}) - 支持执行自动化 Commit/Push\n"
        } ?: "\n[GitHub 授权状态]: 匿名模式 (建议在工坊中绑定 GitHub OAuth 以允许 Agent 执行自动 Commit/Push)\n"

        val formattedPrompt = """
            【办公室协同作业指令 - 指派给：${agent.name}（${agent.roleTitle}）】
            $repoContext$authContext
            【任务内容】：
            $taskPrompt
        """.trimIndent()

        // Update real-time live activity for this agent
        val activityType = when (agent.id) {
            "agent_codex_dev" -> AgentActivityType.COMMITTING
            "agent_devops_ci" -> AgentActivityType.CI_PIPELINE
            "agent_docs_writer" -> AgentActivityType.DOCS_SYNC
            "agent_issue_triage" -> AgentActivityType.ISSUE_TRIAGING
            "agent_scrum_pm" -> AgentActivityType.PR_REVIEWING
            else -> AgentActivityType.IDLE
        }

        updateAgentLiveActivity(
            agent.id,
            AgentLiveActivity(
                agentId = agent.id,
                activityType = activityType,
                activityTitle = "正在执行指令",
                detailLog = taskPrompt.take(60) + if (taskPrompt.length > 60) "..." else "",
                targetRepo = repoName,
                targetBranch = _githubRepoDetails.value?.defaultBranch ?: "main",
                progress = 0.5f,
                isBusy = true
            )
        )

        // Switch to Codex / Office skill or general skill
        val targetSkillId = when (agent.id) {
            "agent_codex_dev", "agent_devops_ci" -> "codex"
            "agent_docs_writer" -> "office"
            "agent_issue_triage", "agent_scrum_pm" -> "automation"
            else -> "general"
        }
        _activeSkillId.value = targetSkillId

        // Send into chat
        sendMessage(formattedPrompt)
    }

    // AI Moments & Skill Sharing Actions
    fun refreshAiMoments() {
        _aiMoments.value = repository.loadAiMoments()
    }

    fun toggleLikeMoment(postId: String) {
        val updated = repository.toggleLikeMoment(postId)
        _aiMoments.value = updated
    }

    fun publishAiMoment(
        title: String,
        content: String,
        category: String,
        relatedSkillId: String? = null,
        githubRepoUrl: String? = null
    ) {
        val relatedSkill = skills.value.firstOrNull { it.id == relatedSkillId }
        val newPost = AiMomentPost(
            id = "moment_" + System.currentTimeMillis(),
            authorName = "Nexus AI 助手",
            authorRole = "自主进化智能体",
            category = category,
            timestamp = System.currentTimeMillis(),
            title = title,
            content = content,
            relatedSkillName = relatedSkill?.name,
            relatedSkillId = relatedSkill?.id,
            githubRepoUrl = githubRepoUrl ?: if (_githubRepo.value.isNotBlank()) "https://github.com/${_githubRepo.value}" else null,
            githubRepoName = _githubRepo.value.ifBlank { null },
            likesCount = 1,
            isLikedByMe = true,
            downloadCount = 0,
            tags = listOf("AI助手", "Nexus", category.replace("_", " ")),
            isPublicToHub = true
        )
        repository.addAiMomentPost(newPost)
        _aiMoments.value = repository.loadAiMoments()
    }

    fun downloadSkillFromMoment(post: AiMomentPost, onComplete: (Boolean, String) -> Unit) {
        repository.incrementMomentDownloadCount(post.id)
        _aiMoments.value = repository.loadAiMoments()

        // If post has a GitHub repo URL, learn and clone skill from it
        if (!post.githubRepoUrl.isNullOrBlank()) {
            learnSkillFromGitHub(post.githubRepoUrl) { success, msg ->
                if (success) {
                    TaskCompletionObserver.notifyTaskCompleted(
                        eventType = TaskCompletedEventType.CHAT_MESSAGE_RESOLVED,
                        taskTitle = "下载并解析技能: ${post.title}",
                        detailSummary = "成功从 AI 朋友圈与开源仓库导入技能",
                        relatedSkillId = post.relatedSkillId,
                        targetRepo = post.githubRepoName,
                        xpGain = 50
                    )
                }
                onComplete(success, msg)
            }
        } else if (post.relatedSkillId != null) {
            _activeSkillId.value = post.relatedSkillId
            onComplete(true, "已切换并激活技能：${post.relatedSkillName ?: post.title}")
        } else {
            onComplete(true, "已同步该动态至技能学习缓存")
        }
    }

    /**
     * 将已掌握的技能一键发布并推送到 GitHub 仓库公开分享
     */
    fun publishSkillToGitHub(skillId: String, onResult: (Boolean, String) -> Unit) {
        val skill = skills.value.firstOrNull { it.id == skillId }
        if (skill == null) {
            onResult(false, "找不到指定的技能定义")
            return
        }

        val parts = _githubRepo.value.split("/")
        if (parts.size != 2) {
            onResult(false, "仓库名称格式不正确，需为 owner/repo")
            return
        }

        _isGitHubLoading.value = true
        viewModelScope.launch {
            val result = repository.publishSkillToGitHubRepo(
                owner = parts[0],
                repo = parts[1],
                skill = skill,
                branch = _githubRepoDetails.value?.defaultBranch ?: "main"
            )
            _isGitHubLoading.value = false

            if (result.isSuccess) {
                val url = result.getOrNull() ?: ""
                // Broadcast to AI Moments
                publishAiMoment(
                    title = "🚀 开源发布技能: 《${skill.name}》",
                    content = "已将技能「${skill.name}」规范与系统指令自动提交至 GitHub 仓库公开共享，支持全网一键下载与实战调用！",
                    category = "GITHUB_RELEASE",
                    relatedSkillId = skill.id,
                    githubRepoUrl = url
                )

                // Trigger task completion observer (+60 XP)
                TaskCompletionObserver.notifyTaskCompleted(
                    eventType = TaskCompletedEventType.GITHUB_COMMIT_PUSH,
                    taskTitle = "发布技能至 GitHub: ${skill.name}",
                    detailSummary = "推送 SKILL.md 至 ${_githubRepo.value}",
                    relatedSkillId = skill.id,
                    targetRepo = _githubRepo.value,
                    xpGain = 60
                )

                onResult(true, "🎉 技能已成功发布到 GitHub 并同步至 AI 朋友圈！\n$url")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "发布技能至 GitHub 失败")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        oauthPollingJob?.cancel()
    }
}
