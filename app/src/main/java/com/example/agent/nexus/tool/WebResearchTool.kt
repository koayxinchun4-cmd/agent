package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import java.net.HttpURLConnection
import java.net.URI

/**
 * Network-backed research primitive. It fetches a user-supplied HTTPS/HTTP URL
 * and returns a bounded text response; it does not claim search-engine results
 * when no search provider is connected.
 */
class WebResearchTool(
    private val connectionFactory: (String) -> HttpURLConnection = ::openConnection
) : AgentTool {
    override val id: String = "web_research"
    override val name: String = "Web Research Agent"
    override val description: String = "Fetches a public web page and returns bounded text for research."

    override suspend fun execute(task: AgentTask): ToolResult {
        val url = extractUrl(task.input)
            ?: return ToolResult.Failure("Web Research 需要一个 http:// 或 https:// URL")

        return runCatching {
            val connection = connectionFactory(url)
            try {
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", USER_AGENT)

                val status = connection.responseCode
                if (status !in 200..299) {
                    return ToolResult.Failure("网页请求失败：HTTP $status")
                }

                val body = connection.inputStream.bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
                    .take(MAX_RESPONSE_CHARS)
                ToolResult.Success("Web Research 完成\nURL：$url\nHTTP：$status\n\n$body")
            } finally {
                connection.disconnect()
            }
        }.getOrElse { ToolResult.Failure("Web Research 执行失败：${it.message ?: "未知错误"}", it) }
    }

    private fun extractUrl(input: String): String? {
        val candidate = input.split(Regex("\\s+"))
            .firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
            ?: return null
        return runCatching {
            val uri = URI(candidate)
            if (uri.host.isNullOrBlank()) null else uri.toString()
        }.getOrNull()
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 12_000
        const val MAX_RESPONSE_CHARS = 50_000
        const val USER_AGENT = "Nexus-AI/1.0 (Android; WebResearchTool)"

        fun openConnection(url: String): HttpURLConnection =
            URI(url).toURL().openConnection() as HttpURLConnection
    }
}
