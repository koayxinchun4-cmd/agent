package com.example.agent.nexus.agent

import com.example.agent.data.remote.Content
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.GeminiRequest
import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.remote.OpenRouterMessage
import com.example.agent.data.remote.OpenRouterRequest
import kotlinx.coroutines.withTimeout

private const val DEFAULT_MODEL_TIMEOUT_MS = 20_000L

class GeminiModelProvider(
    private val apiService: GeminiApiService,
    private val apiKey: String,
    private val timeoutMs: Long = DEFAULT_MODEL_TIMEOUT_MS,
    private val systemPrompt: String = NexusSystemPrompt.text
) : ModelProvider {
    override val id: String = "gemini"
    override val route: ModelRoute = ModelRoute.Gemini
    override val isAvailable: Boolean = apiKey.isRealSecret()

    override suspend fun generate(prompt: String): ModelResponse {
        require(isAvailable) { "Gemini API key is not configured" }
        require(prompt.isNotBlank()) { "Prompt cannot be blank" }

        val request = GeminiRequest(
            systemInstruction = Content(parts = listOf(com.example.agent.data.remote.Part(systemPrompt))),
            contents = listOf(Content(role = "user", parts = listOf(com.example.agent.data.remote.Part(prompt.trim()))))
        )

        return withTimeout(timeoutMs) {
            runCatching { apiService.generateContent(apiKey, request) }
                .getOrElse { error -> throw ProviderRequestException(id, error) }
                .let { response ->
                    val text = response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        ?.trim()
                        .orEmpty()
                    if (text.isBlank()) throw ProviderRequestException(id, "Gemini returned an empty response")
                    ModelResponse(text, id, route)
                }
        }
    }
}

class OpenRouterModelProvider(
    private val apiService: OpenRouterApiService,
    private val apiKey: String,
    private val model: String = "openrouter/auto",
    private val timeoutMs: Long = DEFAULT_MODEL_TIMEOUT_MS
) : ModelProvider {
    override val id: String = "openrouter"
    override val route: ModelRoute = ModelRoute.OpenRouter
    override val isAvailable: Boolean = apiKey.isRealSecret()

    override suspend fun generate(prompt: String): ModelResponse {
        require(isAvailable) { "OpenRouter API key is not configured" }
        require(prompt.isNotBlank()) { "Prompt cannot be blank" }

        val request = OpenRouterRequest(
            model = model,
            messages = listOf(
                OpenRouterMessage("system", NexusSystemPrompt.text),
                OpenRouterMessage("user", prompt.trim())
            )
        )

        return withTimeout(timeoutMs) {
            runCatching {
                apiService.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    referer = "https://github.com/koayxinchun4-cmd/agent",
                    title = "Nexus AI"
                )
            }.getOrElse { error -> throw ProviderRequestException(id, error) }
                .let { response ->
                    val text = response.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?.trim()
                        .orEmpty()
                    if (text.isBlank()) throw ProviderRequestException(id, "OpenRouter returned an empty response")
                    ModelResponse(text, id, route)
                }
        }
    }
}

class ProviderRequestException(
    providerId: String,
    cause: Throwable
) : IllegalStateException("$providerId request failed: ${cause.message ?: cause::class.simpleName}", cause) {
    constructor(providerId: String, message: String) : this(providerId, IllegalStateException(message))
}

private fun String.isRealSecret(): Boolean {
    val value = trim()
    return value.isNotEmpty() &&
        value != "MY_GEMINI_API_KEY" &&
        value != "MY_OPENROUTER_API_KEY" &&
        value.length >= 20
}

object NexusSystemPrompt {
    const val text: String = """
你是 Nexus 智能助手，一个原生 Android 手机上的 AI Agent。
你的定位不是单纯聊天机器人，而是用户的手机 Team Leader：理解目标、拆解任务、选择合适技能，并在获得必要授权后协助执行。
核心约束：你以手机为主要执行环境，不要求电脑，不要求 Root。涉及文件、App、通知、浏览器或其他系统能力时，只能建议或使用 Android 正规授权机制，不绕过系统安全限制。
你服务的用户可能来自马来西亚，因此自然支持繁体中文、Bahasa Melayu 和 English，也允许混合语言。
不要假装已经执行尚未接入的工具。回答时优先给出清晰、可执行的下一步；如果任务需要用户确认或系统权限，先说明。
""".trimIndent()
}
