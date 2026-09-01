package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.local.AgentMemory
import com.example.data.local.AgentSkill
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AgentRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val chatDao = database.chatDao()
    private val skillDao = database.skillDao()
    private val memoryDao = database.memoryDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_agent_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val PREF_SELECTED_MODEL = "selected_gemini_model"
        private const val PREF_TEMPERATURE = "temperature_pref"
        private const val PREF_GITHUB_TOKEN = "custom_github_pat"
        private const val PREF_GITHUB_REPO = "default_github_repo"
        private const val PREF_GITHUB_CLIENT_ID = "github_oauth_client_id"
        private const val PREF_GITHUB_CLIENT_SECRET = "github_oauth_client_secret"
        private const val PREF_WHATSAPP_TOKEN = "whatsapp_cloud_token"
        private const val PREF_WHATSAPP_PHONE_ID = "whatsapp_phone_number_id"
        private const val PREF_WHATSAPP_VERIFY_TOKEN = "whatsapp_verify_token"
        private const val PREF_ADMIN_WHATSAPP_NUMBER = "admin_whatsapp_number"
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
        // Default Client ID for Nexus Agent GitHub OAuth (or user can customize)
        const val DEFAULT_GITHUB_CLIENT_ID = "Iv23li82c0v1NexusDev"
    }

    fun getGitHubToken(): String {
        return prefs.getString(PREF_GITHUB_TOKEN, "") ?: ""
    }

    fun setGitHubToken(token: String) {
        prefs.edit().putString(PREF_GITHUB_TOKEN, token.trim()).apply()
    }

    fun clearGitHubToken() {
        prefs.edit().remove(PREF_GITHUB_TOKEN).apply()
    }

    fun getWhatsAppToken(): String = prefs.getString(PREF_WHATSAPP_TOKEN, "") ?: ""
    fun setWhatsAppToken(token: String) = prefs.edit().putString(PREF_WHATSAPP_TOKEN, token.trim()).apply()

    fun getWhatsAppPhoneId(): String = prefs.getString(PREF_WHATSAPP_PHONE_ID, "") ?: ""
    fun setWhatsAppPhoneId(id: String) = prefs.edit().putString(PREF_WHATSAPP_PHONE_ID, id.trim()).apply()

    fun getWhatsAppVerifyToken(): String = prefs.getString(PREF_WHATSAPP_VERIFY_TOKEN, "my_custom_verify_token_123") ?: "my_custom_verify_token_123"
    fun setWhatsAppVerifyToken(token: String) = prefs.edit().putString(PREF_WHATSAPP_VERIFY_TOKEN, token.trim()).apply()

    fun getAdminWhatsAppNumber(): String = prefs.getString(PREF_ADMIN_WHATSAPP_NUMBER, "") ?: ""
    fun setAdminWhatsAppNumber(number: String) = prefs.edit().putString(PREF_ADMIN_WHATSAPP_NUMBER, number.trim()).apply()

    fun getSkillPracticeCount(skillId: String): Int {
        return prefs.getInt("skill_practice_count_$skillId", 0)
    }

    fun incrementSkillPractice(skillId: String, xpGain: Int = 25) {
        val currentCount = getSkillPracticeCount(skillId)
        val currentXp = getSkillExtraXp(skillId)
        prefs.edit()
            .putInt("skill_practice_count_$skillId", currentCount + 1)
            .putInt("skill_extra_xp_$skillId", currentXp + xpGain)
            .putLong("skill_last_practice_$skillId", System.currentTimeMillis())
            .apply()
    }

    fun getSkillExtraXp(skillId: String): Int {
        return prefs.getInt("skill_extra_xp_$skillId", 0)
    }

    fun getSkillLastPracticeTime(skillId: String): Long {
        return prefs.getLong("skill_last_practice_$skillId", 0L)
    }

    fun boostSkillTraining(skillId: String, xpBoost: Int = 50): Int {
        val currentXp = getSkillExtraXp(skillId)
        val currentCount = getSkillPracticeCount(skillId)
        val newXp = currentXp + xpBoost
        prefs.edit()
            .putInt("skill_extra_xp_$skillId", newXp)
            .putInt("skill_practice_count_$skillId", currentCount + 1)
            .putLong("skill_last_practice_$skillId", System.currentTimeMillis())
            .apply()
        return newXp
    }

    fun getGitHubClientId(): String {
        return prefs.getString(PREF_GITHUB_CLIENT_ID, "") ?: ""
    }

    fun setGitHubClientId(clientId: String) {
        prefs.edit().putString(PREF_GITHUB_CLIENT_ID, clientId.trim()).apply()
    }

    fun getGitHubClientSecret(): String {
        return prefs.getString(PREF_GITHUB_CLIENT_SECRET, "") ?: ""
    }

    fun setGitHubClientSecret(secret: String) {
        prefs.edit().putString(PREF_GITHUB_CLIENT_SECRET, secret.trim()).apply()
    }

    fun getDefaultGitHubRepo(): String {
        return prefs.getString(PREF_GITHUB_REPO, "google/mesop") ?: "google/mesop"
    }

    fun setDefaultGitHubRepo(repo: String) {
        prefs.edit().putString(PREF_GITHUB_REPO, repo.trim()).apply()
    }

    // GitHub OAuth Device Flow (RFC 8628)
    suspend fun requestDeviceCode(clientId: String): Result<com.example.data.api.GitHubDeviceCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = com.example.data.api.GitHubApiClient.oauthService.requestDeviceCode(clientId = clientId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("请求 Device Code 失败: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pollDeviceToken(clientId: String, deviceCode: String): Result<com.example.data.api.GitHubOAuthTokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = com.example.data.api.GitHubApiClient.oauthService.pollDeviceToken(
                clientId = clientId,
                deviceCode = deviceCode
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (!body.accessToken.isNullOrBlank()) {
                    setGitHubToken(body.accessToken)
                }
                Result.success(body)
            } else {
                Result.failure(Exception("轮询 OAuth Token 失败: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exchangeOAuthWebCode(clientId: String, clientSecret: String, code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = com.example.data.api.GitHubApiClient.oauthService.exchangeWebCode(
                clientId = clientId,
                clientSecret = clientSecret,
                code = code,
                redirectUri = "nexus://github-callback"
            )
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.accessToken
                if (!token.isNullOrBlank()) {
                    setGitHubToken(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception(response.body()?.errorDescription ?: "未获取到 Access Token"))
                }
            } else {
                Result.failure(Exception("OAuth Web Code 换取 Token 失败: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOAuthAuthorizeUrl(clientId: String? = null): String {
        val cid = clientId?.ifBlank { null } ?: getGitHubClientId().ifBlank { DEFAULT_GITHUB_CLIENT_ID }
        return "https://github.com/login/oauth/authorize?client_id=$cid&scope=repo,workflow,read:user,user:email&redirect_uri=nexus://github-callback"
    }

    suspend fun fetchCurrentUserProfile(): Result<com.example.data.api.GitHubUserProfile> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("未绑定 GitHub Token"))
            }
            val response = com.example.data.api.GitHubApiClient.service.getCurrentUser("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取用户信息失败 ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserRepositories(): Result<List<com.example.data.api.GitHubRepoResponse>> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("未绑定 GitHub Token"))
            }
            val response = com.example.data.api.GitHubApiClient.service.getUserRepos(
                token = "Bearer $token",
                type = "owner",
                sort = "updated",
                perPage = 50
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取用户仓库列表失败 ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commitAndPushFile(
        owner: String,
        repo: String,
        filePath: String,
        content: String,
        commitMessage: String,
        branch: String? = null
    ): Result<com.example.data.api.GitHubCommitFileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("执行 Git Commit & Push 需要先绑定 GitHub 账号或 Token。"))
            }

            // Check if file already exists on this branch to retrieve its SHA for updates
            var existingSha: String? = null
            try {
                val existingResponse = com.example.data.api.GitHubApiClient.service.getFileContent(
                    owner = owner,
                    repo = repo,
                    path = filePath,
                    ref = branch,
                    token = "Bearer $token"
                )
                if (existingResponse.isSuccessful && existingResponse.body() != null) {
                    existingSha = existingResponse.body()?.sha
                }
            } catch (ignored: Exception) {
                // New file
            }

            val base64Content = android.util.Base64.encodeToString(
                content.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )

            val request = com.example.data.api.GitHubCreateOrUpdateFileRequest(
                message = commitMessage,
                content = base64Content,
                branch = branch,
                sha = existingSha
            )

            val response = com.example.data.api.GitHubApiClient.service.createOrUpdateFile(
                owner = owner,
                repo = repo,
                path = filePath,
                request = request,
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Git Commit & Push 失败 ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPullRequest(
        owner: String,
        repo: String,
        title: String,
        headBranch: String,
        baseBranch: String,
        body: String? = null
    ): Result<com.example.data.api.GitHubPullRequest> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("创建 Pull Request 需要先绑定 GitHub 账号。"))
            }

            val request = com.example.data.api.GitHubCreatePrRequest(
                title = title,
                head = headBranch,
                base = baseBranch,
                body = body
            )

            val response = com.example.data.api.GitHubApiClient.service.createPullRequest(
                owner = owner,
                repo = repo,
                request = request,
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("创建 PR 失败 ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchGitHubRepo(owner: String, repo: String): Result<com.example.data.api.GitHubRepoResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            val authHeader = if (token.isNotBlank()) "Bearer $token" else null
            val response = com.example.data.api.GitHubApiClient.service.getRepo(owner, repo, authHeader)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("GitHub API Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchGitHubIssues(owner: String, repo: String): Result<List<com.example.data.api.GitHubIssue>> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            val authHeader = if (token.isNotBlank()) "Bearer $token" else null
            val response = com.example.data.api.GitHubApiClient.service.getIssues(owner, repo, "open", 10, authHeader)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("GitHub API Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGitHubIssue(
        owner: String,
        repo: String,
        title: String,
        body: String,
        labels: List<String> = emptyList()
    ): Result<com.example.data.api.GitHubIssue> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("创建 GitHub Issue 需要在设置中配置 GitHub Token (PAT)。"))
            }
            val request = com.example.data.api.GitHubCreateIssueRequest(title, body, labels)
            val response = com.example.data.api.GitHubApiClient.service.createIssue(
                owner = owner,
                repo = repo,
                request = request,
                token = "Bearer $token"
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("GitHub Issue 创建失败 ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyGitHubIssue(
        owner: String,
        repo: String,
        issueNumber: Int,
        body: String
    ): Result<com.example.data.api.GitHubCommentResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("回复 Issue 需要在设置中配置 GitHub Token 或通过 OAuth 授权。"))
            }
            val request = com.example.data.api.GitHubIssueCommentRequest(body = body)
            val response = com.example.data.api.GitHubApiClient.service.createIssueComment(
                owner = owner,
                repo = repo,
                issueNumber = issueNumber,
                request = request,
                token = "Bearer $token"
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("回复 Issue 失败 ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeGitHubIssue(
        owner: String,
        repo: String,
        issueNumber: Int
    ): Result<com.example.data.api.GitHubIssue> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            if (token.isBlank()) {
                return@withContext Result.failure(IllegalStateException("关闭 Issue 需要在设置中配置 GitHub Token 或通过 OAuth 授权。"))
            }
            val request = com.example.data.api.GitHubUpdateIssueRequest(state = "closed")
            val response = com.example.data.api.GitHubApiClient.service.updateIssue(
                owner = owner,
                repo = repo,
                issueNumber = issueNumber,
                request = request,
                token = "Bearer $token"
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("关闭 Issue 失败 ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendWhatsAppMessage(
        toNumber: String,
        messageText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = getWhatsAppToken()
            val phoneId = getWhatsAppPhoneId()
            val cleanTo = toNumber.replace("+", "").replace("-", "").replace(" ", "").trim()

            if (token.isBlank() || phoneId.isBlank()) {
                return@withContext Result.failure(IllegalStateException("请先在设置中配置 WhatsApp Token 与 Phone Number ID。"))
            }
            if (cleanTo.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("目标 WhatsApp 手机号码不能为空。"))
            }

            val request = com.example.data.api.WhatsAppSendMessageRequest(
                to = cleanTo,
                text = com.example.data.api.WhatsAppTextMessage(body = messageText)
            )

            val response = com.example.data.api.WhatsAppApiClient.service.sendMessage(
                phoneNumberId = phoneId,
                token = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val msgId = response.body()?.messages?.firstOrNull()?.id ?: "OK"
                Result.success("消息已成功发送至 WhatsApp (ID: $msgId)")
            } else {
                val errBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("WhatsApp API 响应错误 ${response.code()}: $errBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateWhatsAppMicroserviceFiles(
        geminiApiKey: String,
        githubToken: String,
        owner: String,
        repo: String,
        whatsappToken: String,
        phoneNumberId: String,
        verifyToken: String,
        adminNumber: String
    ): Map<String, String> {
        val envContent = """
PORT=3000

# Google AI Studio API Key (https://aistudio.google.com/app/apikey)
GEMINI_API_KEY=${if (geminiApiKey.isNotBlank()) geminiApiKey else "your_gemini_api_key_here"}

# GitHub 配置 (Personal Access Token 或 OAuth Token)
GITHUB_TOKEN=${if (githubToken.isNotBlank()) githubToken else "your_github_personal_access_token_here"}
GITHUB_DEFAULT_OWNER=$owner
GITHUB_DEFAULT_REPO=$repo

# WhatsApp Business Cloud API 配置 (Meta for Developers)
WHATSAPP_TOKEN=${if (whatsappToken.isNotBlank()) whatsappToken else "your_whatsapp_access_token_here"}
WHATSAPP_PHONE_NUMBER_ID=${if (phoneNumberId.isNotBlank()) phoneNumberId else "your_whatsapp_phone_number_id_here"}
WHATSAPP_VERIFY_TOKEN=$verifyToken
ADMIN_WHATSAPP_NUMBER=${if (adminNumber.isNotBlank()) adminNumber else "60123456789"}
""".trimIndent()

        val packageJsonContent = """
{
  "name": "gemini-github-whatsapp",
  "version": "1.0.0",
  "description": "Nexus AI Agent - Gemini + GitHub + WhatsApp Automation Bridge",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "@google/genai": "^0.1.1",
    "@octokit/rest": "^21.1.1",
    "axios": "^1.7.9",
    "dotenv": "^16.4.7",
    "express": "^4.21.2"
  }
}
""".trimIndent()

        val serverJsContent = """
require('dotenv').config();
const express = require('express');
const { GoogleGenAI } = require('@google/genai');
const { Octokit } = require('@octokit/rest');
const axios = require('axios');

const app = express();
app.use(express.json());

// 初始化 API 客户端
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
const octokit = new Octokit({ auth: process.env.GITHUB_TOKEN });

// 定义 Gemini 在 Google AI Studio 中使用的工具 (Function Calling)
const githubTools = [
  {
    name: 'listIssues',
    description: '获取 GitHub 仓库当前的 Issue 列表',
    parameters: {
      type: 'OBJECT',
      properties: {
        owner: { type: 'STRING', description: '仓库拥有者' },
        repo: { type: 'STRING', description: '仓库名' }
      }
    }
  },
  {
    name: 'replyIssue',
    description: '在指定的 GitHub Issue 下发表评论回复',
    parameters: {
      type: 'OBJECT',
      properties: {
        owner: { type: 'STRING', description: '仓库拥有者' },
        repo: { type: 'STRING', description: '仓库名' },
        issue_number: { type: 'NUMBER', description: 'Issue 编号' },
        body: { type: 'STRING', description: '回复内容' }
      },
      required: ['issue_number', 'body']
    }
  },
  {
    name: 'closeIssue',
    description: '关闭指定的 GitHub Issue',
    parameters: {
      type: 'OBJECT',
      properties: {
        owner: { type: 'STRING', description: '仓库拥有者' },
        repo: { type: 'STRING', description: '仓库名' },
        issue_number: { type: 'NUMBER', description: 'Issue 编号' }
      },
      required: ['issue_number']
    }
  }
];

// 执行 GitHub API 操作
async function executeFunction(name, args) {
  const owner = args.owner || process.env.GITHUB_DEFAULT_OWNER;
  const repo = args.repo || process.env.GITHUB_DEFAULT_REPO;

  if (name === 'listIssues') {
    const { data } = await octokit.rest.issues.listForRepo({ owner, repo, state: 'open' });
    return data.map(i => `#${'$'}{i.number}: ${'$'}{i.title}`).join('\n') || '无 Open 状态的 Issue';
  }
  if (name === 'replyIssue') {
    await octokit.rest.issues.createComment({ owner, repo, issue_number: args.issue_number, body: args.body });
    return `成功在 Issue #${'$'}{args.issue_number} 下回复。`;
  }
  if (name === 'closeIssue') {
    await octokit.rest.issues.update({ owner, repo, issue_number: args.issue_number, state: 'closed' });
    return `成功关闭 Issue #${'$'}{args.issue_number}。`;
  }
  return '未知指令';
}

// 发送 WhatsApp 消息
async function sendWhatsAppMessage(to, text) {
  try {
    await axios.post(
      `https://graph.facebook.com/v20.0/${'$'}{process.env.WHATSAPP_PHONE_NUMBER_ID}/messages`,
      {
        messaging_product: 'whatsapp',
        to: to,
        text: { body: text }
      },
      {
        headers: {
          'Authorization': `Bearer ${'$'}{process.env.WHATSAPP_TOKEN}`,
          'Content-Type': 'application/json'
        }
      }
    );
  } catch (err) {
    console.error('发送 WhatsApp 消息失败:', err.response?.data || err.message);
  }
}

// 1. WhatsApp 接入验证 (Meta 初次配置时调用)
app.get('/webhook/whatsapp', (req, res) => {
  const mode = req.query['hub.mode'];
  const token = req.query['hub.verify_token'];
  const challenge = req.query['hub.challenge'];

  if (mode === 'subscribe' && token === process.env.WHATSAPP_VERIFY_TOKEN) {
    res.status(200).send(challenge);
  } else {
    res.sendStatus(403);
  }
});

// 2. 接收来自 WhatsApp 的消息 -> 交给 Gemini 处理 -> 执行 GitHub 操作或回复消息
app.post('/webhook/whatsapp', async (req, res) => {
  res.sendStatus(200);

  const message = req.body.entry?.[0]?.changes?.[0]?.value?.messages?.[0];
  if (!message || message.type !== 'text') return;

  const from = message.from;
  const userPrompt = message.text.body;

  try {
    const model = 'gemini-2.5-flash';
    let response = await ai.models.generateContent({
      model,
      contents: userPrompt,
      config: {
        systemInstruction: '你是一个精通 GitHub 项目管理的 AI 助手。用户通过 WhatsApp 下发指令。如需查改 GitHub 仓库，请主动使用工具函数。',
        tools: [{ functionDeclarations: githubTools }]
      }
    });

    // 如果模型决定发起 Function Calling
    const functionCalls = response.functionCalls;
    if (functionCalls && functionCalls.length > 0) {
      for (const call of functionCalls) {
        const result = await executeFunction(call.name, call.args);
        
        // 将结果回传给模型生成最终语言回复
        response = await ai.models.generateContent({
          model,
          contents: [
            { role: 'user', parts: [{ text: userPrompt }] },
            { role: 'model', parts: [{ functionCall: call }] },
            { role: 'user', parts: [{ functionResponse: { name: call.name, response: { output: result } } }] }
          ]
        });
      }
    }

    const replyText = response.text || '操作已执行完成。';
    await sendWhatsAppMessage(from, replyText);

  } catch (err) {
    console.error('处理 WhatsApp 消息出错:', err);
    await sendWhatsAppMessage(from, '处理您的请求时出现异常。');
  }
});

// 3. 接收来自 GitHub 的事件 (例如新 Issue) -> AI 总结 -> 推送到 WhatsApp
app.post('/webhook/github', async (req, res) => {
  res.sendStatus(200);
  const event = req.headers['x-github-event'];
  
  if (event === 'issues' && req.body.action === 'opened') {
    const issue = req.body.issue;
    const prompt = `GitHub 收到新 Issue：\n标题：${'$'}{issue.title}\n内容：${'$'}{issue.body}\n请简短总结问题。`;

    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: prompt
    });

    const notifyText = `🔔 **GitHub 新 Issue 提醒 (#${'$'}{issue.number})**\n\n${'$'}{response.text}`;
    await sendWhatsAppMessage(process.env.ADMIN_WHATSAPP_NUMBER, notifyText);
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 服务已成功启动！端口号: ${'$'}{PORT}`);
});
""".trimIndent()

        val deployScript = """
#!/bin/bash
echo "📦 正在初始化 Gemini + GitHub + WhatsApp 自动化服务..."
npm init -y
npm install express dotenv @google/genai @octokit/rest axios
echo "✅ 依赖安装完成！使用 'node server.js' 即可启动微服务。"
""".trimIndent()

        return mapOf(
            ".env" to envContent,
            "package.json" to packageJsonContent,
            "server.js" to serverJsContent,
            "setup.sh" to deployScript
        )
    }

    /**
     * 从 GitHub 链接/仓库/SKILL.md 提取并深度学习新技能，支持通过 Gemini 进行元结构语义提取
     */
    suspend fun fetchAndLearnGitHubSkill(inputUrlOrPath: String): Result<com.example.data.api.GitHubLearnedSkillParsed> = withContext(Dispatchers.IO) {
        try {
            val token = getGitHubToken()
            val authHeader = if (token.isNotBlank()) "Bearer $token" else null

            var rawMarkdown = ""
            var resolvedSource = inputUrlOrPath.trim()

            // 1. If it is a full URL (github.com or raw.githubusercontent.com)
            if (inputUrlOrPath.startsWith("http://") || inputUrlOrPath.startsWith("https://")) {
                var targetFetchUrl = inputUrlOrPath
                if (inputUrlOrPath.contains("github.com/") && !inputUrlOrPath.contains("raw.githubusercontent.com")) {
                    if (inputUrlOrPath.contains("/blob/")) {
                        targetFetchUrl = inputUrlOrPath.replace("github.com/", "raw.githubusercontent.com/").replace("/blob/", "/")
                    }
                }
                val rawResp = com.example.data.api.GitHubApiClient.service.getRawContent(targetFetchUrl, authHeader)
                if (rawResp.isSuccessful && rawResp.body() != null) {
                    rawMarkdown = rawResp.body()!!.string()
                } else {
                    return@withContext Result.failure(Exception("无法从指定 URL 下载技能文件: HTTP ${rawResp.code()}"))
                }
            } else {
                // 2. Format: owner/repo or owner/repo/path/to/SKILL.md
                val cleaned = inputUrlOrPath.removePrefix("github.com/").trim('/')
                val parts = cleaned.split("/")
                if (parts.size < 2) {
                    return@withContext Result.failure(IllegalArgumentException("请输入有效的 GitHub 仓库 (如 owner/repo) 或具体文件路径 (如 owner/repo/skills/SKILL.md)"))
                }

                val owner = parts[0]
                val repo = parts[1]
                val customFilePath = if (parts.size > 2) parts.subList(2, parts.size).joinToString("/") else null

                // Try candidates: specified path -> SKILL.md -> .github/skills/SKILL.md -> README.md
                val candidatePaths = if (customFilePath != null) {
                    listOf(customFilePath)
                } else {
                    listOf("SKILL.md", ".github/skills/SKILL.md", "skills/SKILL.md", "README.md")
                }

                var fetchedSuccessfully = false
                for (path in candidatePaths) {
                    try {
                        val fileResp = com.example.data.api.GitHubApiClient.service.getFileContent(
                            owner = owner,
                            repo = repo,
                            path = path,
                            token = authHeader
                        )
                        if (fileResp.isSuccessful && fileResp.body() != null) {
                            val fileContent = fileResp.body()!!
                            if (!fileContent.content.isNullOrBlank()) {
                                val decodedBytes = android.util.Base64.decode(
                                    fileContent.content.replace("\n", "").trim(),
                                    android.util.Base64.DEFAULT
                                )
                                rawMarkdown = String(decodedBytes, Charsets.UTF_8)
                                resolvedSource = "$owner/$repo/$path"
                                fetchedSuccessfully = true
                                break
                            } else if (!fileContent.downloadUrl.isNullOrBlank()) {
                                val rawResp = com.example.data.api.GitHubApiClient.service.getRawContent(fileContent.downloadUrl, authHeader)
                                if (rawResp.isSuccessful && rawResp.body() != null) {
                                    rawMarkdown = rawResp.body()!!.string()
                                    resolvedSource = "$owner/$repo/$path"
                                    fetchedSuccessfully = true
                                    break
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                        // continue to next candidate
                    }
                }

                if (!fetchedSuccessfully || rawMarkdown.isBlank()) {
                    return@withContext Result.failure(Exception("未能从仓库 $owner/$repo 中找到可解析的技能定义文件 (尝试了 SKILL.md 与 README.md)。"))
                }
            }

            // 3. Use Gemini AI to extract structured skill parameters
            val apiKey = getApiKey()
            var parsedSkill: com.example.data.api.GitHubLearnedSkillParsed? = null

            if (apiKey.isNotBlank()) {
                try {
                    val prompt = """
                        请从以下从 GitHub 获取的技能/工具/仓库文档 (SKILL.md 或 README.md) 中解析并提炼为一个用于智能体（AI Agent）的高性能 System Skill。
                        
                        请严格按以下 JSON 结构输出（不要添加任何 markdown 代码块标记以外的文字）：
                        ```json
                        {
                          "name": "技能名称 (中英文均可，简洁专业，如：Docker 容器构建师、Spring Boot 架构师、GraphQL 专家)",
                          "description": "简明扼要的一句话功能介绍（30字以内）",
                          "category": "coding 或 office 或 automation 或 general",
                          "systemPrompt": "提取提炼该技能的完整 System Prompt、核心规范、专业背景及执行规则，确保智能体使用该技能时具备极强的专业度和行为约束",
                          "samplePrompts": [
                            "2-3 个最具代表性的用户输入触发示例"
                          ]
                        }
                        ```
                        
                        【文档原始内容】：
                        ${rawMarkdown.take(6000)}
                    """.trimIndent()

                    val geminiReq = GeminiRequest(
                        contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 2048)
                    )
                    val geminiResp = GeminiApiClient.service.generateContent(
                        model = getSelectedModel(),
                        apiKey = apiKey,
                        request = geminiReq
                    )
                    if (geminiResp.isSuccessful && geminiResp.body() != null) {
                        val aiText = geminiResp.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                        val jsonBlock = aiText.substringAfter("```json").substringBefore("```").trim()
                            .ifBlank { aiText.substringAfter("{").substringBeforeLast("}").let { "{$it}" } }
                        
                        val jsonObject = org.json.JSONObject(jsonBlock)
                        val name = jsonObject.optString("name", "GitHub 习得技能")
                        val description = jsonObject.optString("description", "从 GitHub 自动学习的智能体技能")
                        val category = jsonObject.optString("category", "coding")
                        val sysPrompt = jsonObject.optString("systemPrompt", rawMarkdown.take(1500))
                        val samples = mutableListOf<String>()
                        val samplesJson = jsonObject.optJSONArray("samplePrompts")
                        if (samplesJson != null) {
                            for (i in 0 until samplesJson.length()) {
                                samples.add(samplesJson.getString(i))
                            }
                        }
                        parsedSkill = com.example.data.api.GitHubLearnedSkillParsed(
                            name = name,
                            description = description,
                            systemPrompt = sysPrompt,
                            category = category,
                            samplePrompts = samples,
                            sourceRepoOrUrl = resolvedSource,
                            rawContentLength = rawMarkdown.length
                        )
                    }
                } catch (ignored: Exception) {
                    // Fallback to local heuristic parsing
                }
            }

            // Fallback: Local parsing if Gemini was unavailable or failed
            if (parsedSkill == null) {
                val lines = rawMarkdown.lines()
                val titleLine = lines.firstOrNull { it.startsWith("# ") }?.removePrefix("# ")?.trim()
                    ?: lines.firstOrNull { it.isNotBlank() }?.take(30) ?: "GitHub 习得技能"
                val descLine = lines.drop(1).firstOrNull { it.isNotBlank() && !it.startsWith("#") }?.take(80)
                    ?: "从 $resolvedSource 自动解析学习的技能扩展"

                parsedSkill = com.example.data.api.GitHubLearnedSkillParsed(
                    name = titleLine.take(30),
                    description = descLine,
                    systemPrompt = "你是一名专门掌握以下 GitHub 技能规范的 AI 专家：\n\n$rawMarkdown",
                    category = "coding",
                    samplePrompts = listOf("请根据从 GitHub 学习的 $titleLine 规范，为我提供专业指导与代码示范"),
                    sourceRepoOrUrl = resolvedSource,
                    rawContentLength = rawMarkdown.length
                )
            }

            Result.success(parsedSkill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOfficeAgentsList(): List<com.example.data.api.OfficeAgent> {
        return listOf(
            com.example.data.api.OfficeAgent(
                id = "agent_cto_new",
                name = "Nexus CTO.new 架构师",
                roleTitle = "自主全栈首席技术官 (免配置 API)",
                avatarEmoji = "🏛️",
                description = "具备端到端软件生命周期自主决策能力：架构选型、模块设计、技术评审、拆解 Sprint 与指导全栈落地。",
                capabilities = listOf("全栈架构设计", "免配置自主引擎", "技术选型决策", "代码审查与重构", "Sprint 里程碑"),
                defaultPromptTemplate = "作为 CTO.new 自主全栈首席技术官，请为项目【%REPO%】规划完整的技术架构演进方案与工程落地方案："
            ),
            com.example.data.api.OfficeAgent(
                id = "agent_codex_dev",
                name = "Codex 研发助理",
                roleTitle = "GitHub 核心开发与 PR 审查员",
                avatarEmoji = "👨‍💻",
                description = "审核 GitHub PR、生成重构补丁、检查代码健壮性并提供标准 Git Commit 规范。",
                capabilities = listOf("PR 代码审查", "Bug 修复补丁", "Commit 规范生成", "Kotlin/Python"),
                defaultPromptTemplate = "请帮我为 GitHub 仓库【%REPO%】执行代码审查与架构优化，重点关注异常处理与性能瓶颈："
            ),
            com.example.data.api.OfficeAgent(
                id = "agent_docs_writer",
                name = "文档与周报助理",
                roleTitle = "Docs & Technical Writer",
                avatarEmoji = "📝",
                description = "专精 GitHub 规范 README.md、API 文档、Release 变更日志以及项目周报整理。",
                capabilities = listOf("README 生成", "API 文档撰写", "Release Notes", "办公表格"),
                defaultPromptTemplate = "请为 GitHub 项目【%REPO%】编写一份结构清晰、包含特性列表与快速开始指令的专业 README.md："
            ),
            com.example.data.api.OfficeAgent(
                id = "agent_devops_ci",
                name = "DevOps 运维专家",
                roleTitle = "GitHub Actions 编排工程师",
                avatarEmoji = "🚀",
                description = "自动化编写 GitHub Actions 工作流 (.github/workflows/ci.yml)、Docker 容器构建及发布流程。",
                capabilities = listOf("GitHub Actions", "CI/CD 编排", "Dockerfile", "构建脚本"),
                defaultPromptTemplate = "请为 GitHub 仓库【%REPO%】编写一个包含自动化测试、Gradle 构建与发布产物的 GitHub Actions 工作流 (ci.yml)："
            ),
            com.example.data.api.OfficeAgent(
                id = "agent_issue_triage",
                name = "Issue 巡检官",
                roleTitle = "Bug 排查与需求分流",
                avatarEmoji = "🐛",
                description = "分析 GitHub 上的 Open Issues、定位异常崩溃根因并提供完整的重现代码与解决方案。",
                capabilities = listOf("Issue 分析", "Bug 定位", "测试用例生成", "异常排查"),
                defaultPromptTemplate = "请帮我分析 GitHub 仓库【%REPO%】的当前 Issues，定位常见 Bug 根因并给出修复建议："
            ),
            com.example.data.api.OfficeAgent(
                id = "agent_scrum_pm",
                name = "敏捷项目经理",
                roleTitle = "Scrum Master & Architect",
                avatarEmoji = "📋",
                description = "拆解产品需求为 GitHub Milestones、排定 Sprint 任务优先级并生成开发燃尽计划。",
                capabilities = listOf("需求拆解", "里程碑规划", "任务看板", "Sprint 评估"),
                defaultPromptTemplate = "请将针对 GitHub 仓库【%REPO%】的最新功能规划拆解为 3 个 Sprint 的敏捷任务与 Milestone 计划表："
            )
        )
    }

    fun getApiKey(): String {
        val customKey = prefs.getString(PREF_CUSTOM_API_KEY, "") ?: ""
        if (customKey.isNotBlank()) return customKey.trim()

        val buildKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(PREF_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(PREF_SELECTED_MODEL, model).apply()
    }

    fun getTemperature(): Float {
        return prefs.getFloat(PREF_TEMPERATURE, 0.7f)
    }

    fun setTemperature(temp: Float) {
        prefs.edit().putFloat(PREF_TEMPERATURE, temp).apply()
    }

    // Sessions & Messages
    fun getAllSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()

    suspend fun createNewSession(skillId: String? = "general", title: String? = null): ChatSession {
        val id = UUID.randomUUID().toString()
        val skill = if (skillId != null) skillDao.getSkillById(skillId) else null
        val defaultTitle = title ?: (skill?.name ?: "新对话")
        val session = ChatSession(
            id = id,
            title = defaultTitle,
            activeSkillId = skillId,
            systemPrompt = skill?.systemPrompt
        )
        chatDao.insertOrUpdateSession(session)
        return session
    }

    suspend fun getSession(sessionId: String): ChatSession? = chatDao.getSessionById(sessionId)

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun deleteSession(session: ChatSession) {
        chatDao.deleteMessagesForSession(session.id)
        chatDao.deleteSession(session)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    // Skills
    fun getAllSkills(): Flow<List<AgentSkill>> = skillDao.getAllSkills()

    suspend fun getSkillById(id: String): AgentSkill? = skillDao.getSkillById(id)

    suspend fun saveCustomSkill(skill: AgentSkill) {
        skillDao.insertSkill(skill)
    }

    suspend fun deleteSkill(skill: AgentSkill) {
        if (!skill.isBuiltIn) {
            skillDao.deleteSkill(skill)
        }
    }

    // Memories & Long-term Context Retention
    fun getAllMemories(): Flow<List<AgentMemory>> = memoryDao.getAllMemories()

    fun getMemoriesByCategory(category: String): Flow<List<AgentMemory>> = memoryDao.getMemoriesByCategory(category)

    fun searchMemories(query: String): Flow<List<AgentMemory>> = memoryDao.searchMemories(query)

    suspend fun addMemory(key: String, content: String, category: String = "custom", importance: Int = 1) {
        val existing = memoryDao.getMemoryByKey(key)
        if (existing != null) {
            memoryDao.updateMemory(
                existing.copy(
                    content = content,
                    category = category,
                    importance = importance,
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
        } else {
            memoryDao.insertMemory(
                AgentMemory(
                    key = key,
                    content = content,
                    category = category,
                    importance = importance,
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateMemory(memory: AgentMemory) {
        memoryDao.updateMemory(memory.copy(lastAccessedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMemory(memory: AgentMemory) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(memoryId: Long) {
        memoryDao.deleteMemoryById(memoryId)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }

    // Chat History Search & Maintenance
    fun searchChatMessages(query: String): Flow<List<ChatMessage>> = chatDao.searchMessages(query)

    suspend fun deleteSessionById(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    // Chat Inference
    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        activeSkillId: String?
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            // Save user message in DB
            val userMsg = ChatMessage(
                sessionId = sessionId,
                role = "user",
                content = userPrompt,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMsg)

            // Update session updated timestamp
            val session = chatDao.getSessionById(sessionId)
            if (session != null) {
                val newTitle = if (session.messageCount == 0 && userPrompt.length > 3) {
                    userPrompt.take(28).trim() + if (userPrompt.length > 28) "..." else ""
                } else session.title
                chatDao.insertOrUpdateSession(
                    session.copy(
                        title = newTitle,
                        updatedAt = System.currentTimeMillis(),
                        messageCount = session.messageCount + 1
                    )
                )
            }

            val apiKey = getApiKey()
            val skill = activeSkillId?.let { skillDao.getSkillById(it) }

            // If API Key is not configured, seamlessly execute via CTO.new Autonomous Engine
            if (apiKey.isBlank()) {
                val assignedAgent = getOfficeAgentsList().find { it.id == activeSkillId }
                val autonomousText = com.example.data.local.AutonomousEngineLocalPresets.generateAutonomousResponse(
                    userPrompt = userPrompt,
                    activeSkillId = activeSkillId,
                    currentRepo = getDefaultGitHubRepo(),
                    currentBranch = "main",
                    assignedAgent = assignedAgent
                )

                val actionType = detectActionType(autonomousText, skill?.category)
                val assistantMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = autonomousText,
                    timestamp = System.currentTimeMillis(),
                    skillNameUsed = skill?.name ?: (assignedAgent?.name ?: "Nexus CTO.new"),
                    actionType = actionType,
                    tokenCount = 512
                )
                chatDao.insertMessage(assistantMsg)

                if (activeSkillId != null) {
                    incrementSkillPractice(activeSkillId, 40)
                }

                val updatedSession = chatDao.getSessionById(sessionId)
                if (updatedSession != null) {
                    chatDao.insertOrUpdateSession(
                        updatedSession.copy(
                            updatedAt = System.currentTimeMillis(),
                            messageCount = updatedSession.messageCount + 1
                        )
                    )
                }

                return@withContext Result.success(assistantMsg)
            }

            // Prepare history & context
            val history = chatDao.getMessagesListForSession(sessionId)
            val memories = memoryDao.getMemoriesList()

            // Build System Instruction with Active Skill & Local Memory
            val memoryContext = if (memories.isNotEmpty()) {
                "\n[本地长期记忆与用户画像]:\n" + memories.joinToString("\n") { "- ${it.key}: ${it.content}" }
            } else ""

            val baseSkillPrompt = skill?.systemPrompt
                ?: "你是一个运行在 Android 设备上的全能移动端 AI 智能助手（Nexus Agent）。请使用中文（华文）进行专业、清晰、准确的回答。"

            val fullSystemPrompt = "$baseSkillPrompt\n$memoryContext\n\n请始终使用规范的中文与格式优美的 Markdown 排版进行回答。"

            val systemInstruction = Content(
                parts = listOf(Part(text = fullSystemPrompt))
            )

            // Build API request contents from history (limited to last 16 turns to avoid context overflow)
            val contentsList = history.takeLast(16).map { msg ->
                val role = if (msg.role == "user") "user" else "model"
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.content))
                )
            }

            val request = GeminiRequest(
                contents = contentsList,
                generationConfig = GenerationConfig(
                    temperature = getTemperature(),
                    maxOutputTokens = 4096
                ),
                systemInstruction = systemInstruction
            )

            val modelName = getSelectedModel()
            val response = GeminiApiClient.service.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val responseText = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Gemini 未返回文本内容。"
                val tokens = body.usageMetadata?.totalTokenCount ?: 0

                val actionType = detectActionType(responseText, skill?.category)

                val assistantMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = responseText,
                    timestamp = System.currentTimeMillis(),
                    skillNameUsed = skill?.name,
                    actionType = actionType,
                    tokenCount = tokens
                )
                chatDao.insertMessage(assistantMsg)

                // Record skill usage & mastery XP growth
                if (activeSkillId != null) {
                    incrementSkillPractice(activeSkillId, 35)
                }

                // Update session count again
                val updatedSession = chatDao.getSessionById(sessionId)
                if (updatedSession != null) {
                    chatDao.insertOrUpdateSession(
                        updatedSession.copy(
                            updatedAt = System.currentTimeMillis(),
                            messageCount = updatedSession.messageCount + 1
                        )
                    )
                }

                return@withContext Result.success(assistantMsg)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "API 错误 ${response.code()}: ${response.message()}"
                val fallbackMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = "⚠️ Gemini API 调用异常:\n$errorMsg\n\n请在右上角「设置」中检查 API Key 或网络连接状态。",
                    timestamp = System.currentTimeMillis(),
                    error = true
                )
                chatDao.insertMessage(fallbackMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorChatMessage = ChatMessage(
                sessionId = sessionId,
                role = "model",
                content = "⚠️ 请求发送失败: ${e.localizedMessage ?: "网络连接异常"}\n请检查您的网络连接并在设置中确认 API Key。",
                timestamp = System.currentTimeMillis(),
                error = true
            )
            chatDao.insertMessage(errorChatMessage)
            return@withContext Result.failure(e)
        }
    }

    private fun detectActionType(text: String, skillCategory: String?): String {
        return when {
            skillCategory == "office" || text.contains("| --- |") || text.contains("Subject:") || text.contains("主题:") -> "office"
            skillCategory == "coding" || text.contains("```kotlin") || text.contains("```python") || text.contains("```bash") -> "code"
            skillCategory == "automation" || text.contains("am start") || text.contains("Intent") || text.contains("Cron:") -> "auto"
            else -> "none"
        }
    }

    // Export chat to ZIP file containing markdown and raw text transcripts
    suspend fun exportChatToZip(sessionId: String): File = withContext(Dispatchers.IO) {
        val session = chatDao.getSessionById(sessionId)
        val messages = chatDao.getMessagesListForSession(sessionId)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val stringBuilder = StringBuilder()
        stringBuilder.append("# 对话记录导出: ${session?.title ?: "Nexus 会话"}\n")
        stringBuilder.append("导出时间: ${dateFormat.format(Date())}\n")
        stringBuilder.append("消息总数: ${messages.size}\n\n---\n\n")

        for (msg in messages) {
            val sender = if (msg.role == "user") "👤 用户 (User)" else "🤖 Nexus 智能体"
            val time = dateFormat.format(Date(msg.timestamp))
            stringBuilder.append("### $sender ($time)\n")
            if (msg.skillNameUsed != null) {
                stringBuilder.append("*[使用技能: ${msg.skillNameUsed}]*\n\n")
            }
            stringBuilder.append("${msg.content}\n\n---\n\n")
        }

        val transcriptText = stringBuilder.toString()

        // Create Zip File
        val exportDir = File(context.cacheDir, "chat_exports").apply { mkdirs() }
        val zipFile = File(exportDir, "chat_export_${System.currentTimeMillis()}.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Add Markdown transcript
            val mdEntry = ZipEntry("chat_transcript.md")
            zos.putNextEntry(mdEntry)
            zos.write(transcriptText.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // Add Plain Text transcript
            val txtEntry = ZipEntry("chat_transcript.txt")
            zos.putNextEntry(txtEntry)
            zos.write(transcriptText.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // Add metadata json
            val metaJson = """
                {
                    "sessionId": "$sessionId",
                    "title": "${session?.title?.replace("\"", "\\\"") ?: "Nexus Session"}",
                    "messageCount": ${messages.size},
                    "exportedAt": "${dateFormat.format(Date())}"
                }
            """.trimIndent()
            val metaEntry = ZipEntry("metadata.json")
            zos.putNextEntry(metaEntry)
            zos.write(metaJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        return@withContext zipFile
    }

    // AI Moments (智能体朋友圈 & 社区技能动态)
    private val PREF_AI_MOMENTS = "ai_moments_posts_json"

    fun loadAiMoments(): List<com.example.data.local.AiMomentPost> {
        val json = prefs.getString(PREF_AI_MOMENTS, null)
        if (json.isNullOrBlank()) {
            val initial = getInitialAiMoments()
            saveAiMoments(initial)
            return initial
        }
        return try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<com.example.data.local.AiMomentPost>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val tagsArr = obj.optJSONArray("tags")
                val tagsList = mutableListOf<String>()
                if (tagsArr != null) {
                    for (j in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(j))
                    }
                }
                list.add(
                    com.example.data.local.AiMomentPost(
                        id = obj.getString("id"),
                        authorName = obj.getString("authorName"),
                        authorRole = obj.getString("authorRole"),
                        authorAvatarUrl = obj.optString("authorAvatarUrl", ""),
                        category = obj.getString("category"),
                        timestamp = obj.getLong("timestamp"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        relatedSkillName = obj.optString("relatedSkillName").takeIf { it.isNotBlank() },
                        relatedSkillId = obj.optString("relatedSkillId").takeIf { it.isNotBlank() },
                        githubRepoUrl = obj.optString("githubRepoUrl").takeIf { it.isNotBlank() },
                        githubRepoName = obj.optString("githubRepoName").takeIf { it.isNotBlank() },
                        likesCount = obj.optInt("likesCount", 0),
                        isLikedByMe = obj.optBoolean("isLikedByMe", false),
                        downloadCount = obj.optInt("downloadCount", 0),
                        tags = tagsList,
                        isPublicToHub = obj.optBoolean("isPublicToHub", true),
                        skillJsonSnapshot = obj.optString("skillJsonSnapshot").takeIf { it.isNotBlank() }
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            getInitialAiMoments()
        }
    }

    fun saveAiMoments(posts: List<com.example.data.local.AiMomentPost>) {
        val array = org.json.JSONArray()
        for (p in posts) {
            val obj = org.json.JSONObject().apply {
                put("id", p.id)
                put("authorName", p.authorName)
                put("authorRole", p.authorRole)
                put("authorAvatarUrl", p.authorAvatarUrl)
                put("category", p.category)
                put("timestamp", p.timestamp)
                put("title", p.title)
                put("content", p.content)
                put("relatedSkillName", p.relatedSkillName ?: "")
                put("relatedSkillId", p.relatedSkillId ?: "")
                put("githubRepoUrl", p.githubRepoUrl ?: "")
                put("githubRepoName", p.githubRepoName ?: "")
                put("likesCount", p.likesCount)
                put("isLikedByMe", p.isLikedByMe)
                put("downloadCount", p.downloadCount)
                val tagsArr = org.json.JSONArray()
                p.tags.forEach { tagsArr.put(it) }
                put("tags", tagsArr)
                put("isPublicToHub", p.isPublicToHub)
                put("skillJsonSnapshot", p.skillJsonSnapshot ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString(PREF_AI_MOMENTS, array.toString()).apply()
    }

    fun addAiMomentPost(post: com.example.data.local.AiMomentPost) {
        val current = loadAiMoments().toMutableList()
        current.add(0, post)
        saveAiMoments(current)
    }

    fun toggleLikeMoment(postId: String): List<com.example.data.local.AiMomentPost> {
        val current = loadAiMoments().map { p ->
            if (p.id == postId) {
                val newLiked = !p.isLikedByMe
                val newCount = if (newLiked) p.likesCount + 1 else maxOf(0, p.likesCount - 1)
                p.copy(isLikedByMe = newLiked, likesCount = newCount)
            } else p
        }
        saveAiMoments(current)
        return current
    }

    fun incrementMomentDownloadCount(postId: String) {
        val current = loadAiMoments().map { p ->
            if (p.id == postId) p.copy(downloadCount = p.downloadCount + 1) else p
        }
        saveAiMoments(current)
    }

    private fun getInitialAiMoments(): List<com.example.data.local.AiMomentPost> {
        val now = System.currentTimeMillis()
        return listOf(
            com.example.data.local.AiMomentPost(
                id = "moment_codex_01",
                authorName = "Codex Dev Agent",
                authorRole = "代码架构师",
                category = "GITHUB_RELEASE",
                timestamp = now - 15 * 60 * 1000,
                title = "🚀 已向 GitHub 提交 Android 响应式架构补丁",
                content = "刚刚自动完成了 M3 动态主题与 Room 数据库离线缓存模块的集成，并向 main 分支推送了 Commit。\n公开仓库地址支持随时克隆使用与 Fork！",
                githubRepoUrl = "https://github.com/google/mesop",
                githubRepoName = "google/mesop",
                relatedSkillName = "Android 极客架构师",
                relatedSkillId = "codex",
                likesCount = 18,
                downloadCount = 42,
                tags = listOf("GitHub", "Commit", "Android", "Kotlin")
            ),
            com.example.data.local.AiMomentPost(
                id = "moment_gemini_skill_02",
                authorName = "Nexus 进化母体",
                authorRole = "技能演化中枢",
                category = "SKILL_SHARE",
                timestamp = now - 2 * 3600 * 1000,
                title = "✨ 开源技能发布：《多模态文档提炼专家》",
                content = "本技能已完成 120 次实战进化，具备自动结构化 Markdown、生成会议纪要与敏捷行动项的能力。支持一键克隆至本地技能库使用！",
                githubRepoUrl = "https://github.com/tensorflow/tensorflow",
                githubRepoName = "tensorflow/tensorflow",
                relatedSkillName = "办公文档极速处理",
                relatedSkillId = "office",
                likesCount = 35,
                downloadCount = 89,
                tags = listOf("技能开源", "Prompt", "文档助手")
            ),
            com.example.data.local.AiMomentPost(
                id = "moment_devops_03",
                authorName = "DevOps 运维特工",
                authorRole = "自动化流水线管家",
                category = "TASK_ACCOMPLISHED",
                timestamp = now - 8 * 3600 * 1000,
                title = "⚡ 成功跑通 GitHub Actions CI/CD 流水线",
                content = "检测到 3 项单元测试并全数通过，产出 APK 构建工件！自动化技能已同步自增 45 XP 熟练度。",
                githubRepoUrl = "https://github.com/google/mesop",
                githubRepoName = "google/mesop",
                relatedSkillName = "自动化与系统运维",
                relatedSkillId = "automation",
                likesCount = 22,
                downloadCount = 16,
                tags = listOf("CI/CD", "GitHub Actions", "实战突破")
            )
        )
    }

    /**
     * 将技能导出并推送到 GitHub 仓库公开分享 (作为 SKILL.md)
     */
    suspend fun publishSkillToGitHubRepo(
        owner: String,
        repo: String,
        skill: AgentSkill,
        branch: String = "main"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val skillMdContent = """
                ---
                name: ${skill.name}
                description: ${skill.description}
                category: ${skill.category}
                version: 1.0.0
                author: Nexus AI Agent Community
                published_at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                ---
                
                # ${skill.name}
                
                ${skill.description}
                
                ## 🎯 系统指令 (System Prompt)
                
                ```markdown
                ${skill.systemPrompt}
                ```
                
                ## 💡 示例调用指令 (Sample Prompts)
                
                ${skill.samplePromptsJson}
                
                ---
                *Exported and published by Nexus Android AI Agent.*
            """.trimIndent()

            val filePath = ".agents/skills/${skill.id.replace(" ", "_")}/SKILL.md"
            val commitMsg = "feat(skills): publish '${skill.name}' skill definition to GitHub repository"

            val result = commitAndPushFile(
                owner = owner,
                repo = repo,
                filePath = filePath,
                content = skillMdContent,
                commitMessage = commitMsg,
                branch = branch
            )

            if (result.isSuccess) {
                val repoUrl = "https://github.com/$owner/$repo/blob/$branch/$filePath"
                Result.success(repoUrl)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("推送到 GitHub 失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

