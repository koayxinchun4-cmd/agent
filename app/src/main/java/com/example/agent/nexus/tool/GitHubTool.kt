package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import java.net.HttpURLConnection
import java.net.URI

/**
 * Read-only GitHub Agent primitive. It queries the public GitHub REST API for
 * a repository and returns bounded metadata; write operations require a future
 * explicit authenticated integration.
 */
class GitHubTool(
    private val connectionFactory: (String) -> HttpURLConnection = ::openConnection
) : AgentTool {
    override val id: String = "github"
    override val name: String = "GitHub Agent"
    override val description: String = "Reads public GitHub repository metadata through the REST API."

    override suspend fun execute(task: AgentTask): ToolResult {
        val repository = extractRepository(task.input)
            ?: return ToolResult.Failure("GitHub Agent 需要 owner/repository，例如 koayxinchun4-cmd/agent")
        val url = "https://api.github.com/repos/$repository"

        return runCatching {
            val connection = connectionFactory(url)
            try {
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("User-Agent", USER_AGENT)
                val status = connection.responseCode
                if (status !in 200..299) return ToolResult.Failure("GitHub 请求失败：HTTP $status")
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                    .take(MAX_RESPONSE_CHARS)
                ToolResult.Success("GitHub Agent 完成\nRepository：$repository\nHTTP：$status\n\n$body")
            } finally {
                connection.disconnect()
            }
        }.getOrElse { ToolResult.Failure("GitHub Agent 执行失败：${it.message ?: "未知错误"}", it) }
    }

    private fun extractRepository(input: String): String? {
        val candidate = input.split(Regex("\\s+"))
            .map { it.trim().trimEnd('.', ',', '。', '，') }
            .firstOrNull { it.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) }
            ?: return null
        return candidate
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 12_000
        const val MAX_RESPONSE_CHARS = 50_000
        const val USER_AGENT = "Nexus-AI/1.0 (Android; GitHubTool)"

        fun openConnection(url: String): HttpURLConnection =
            URI(url).toURL().openConnection() as HttpURLConnection
    }
}
