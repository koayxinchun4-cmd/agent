package com.example.agent.nexus.tool

import com.example.agent.data.local.AgentMemory
import com.example.agent.data.local.AgentMemoryDao
import com.example.agent.nexus.agent.AgentTask

/**
 * Local-only memory tool backed by Room. It supports explicit save, recall,
 * and recent-memory listing without sending memory data to a server.
 */
class MemoryTool(
    private val dao: AgentMemoryDao
) : AgentTool {
    override val id: String = "memory"
    override val name: String = "Nexus Memory"
    override val description: String = "Saves, recalls, or lists Nexus local memories."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("记忆任务内容不能为空")

        return runCatching {
            when {
                input.startsWith("记住") || input.startsWith("remember", ignoreCase = true) -> save(input)
                input.startsWith("查记忆") || input.startsWith("recall", ignoreCase = true) -> recall(input)
                else -> recent()
            }
        }.getOrElse { ToolResult.Failure("记忆工具执行失败：${it.message ?: "未知错误"}", it) }
    }

    private suspend fun save(input: String): ToolResult {
        val content = input.removePrefix("记住").removePrefix("remember").trim()
        if (content.isEmpty()) return ToolResult.Failure("请提供要保存的内容")

        val separator = content.indexOf(':').takeIf { it >= 0 } ?: content.indexOf('：')
        val key = if (separator > 0) content.substring(0, separator).trim() else "general"
        val value = if (separator > 0) content.substring(separator + 1).trim() else content
        if (value.isEmpty()) return ToolResult.Failure("请提供要保存的内容")

        dao.insert(AgentMemory(key = key.ifEmpty { "general" }, value = value))
        return ToolResult.Success("已保存记忆\nKey: ${key.ifEmpty { "general" }}\nValue: $value")
    }

    private suspend fun recall(input: String): ToolResult {
        val query = input.removePrefix("查记忆").removePrefix("recall").trim()
        val memories = if (query.isEmpty()) dao.getRecent() else dao.search(query)
        if (memories.isEmpty()) return ToolResult.Success("没有找到相关记忆。")
        return ToolResult.Success(
            "Nexus 记忆\n" + memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
        )
    }

    private suspend fun recent(): ToolResult {
        val memories = dao.getRecent()
        if (memories.isEmpty()) return ToolResult.Success("Nexus 目前没有保存的记忆。")
        return ToolResult.Success(
            "最近的 Nexus 记忆\n" + memories.take(20).joinToString("\n") { "- ${it.key}: ${it.value}" }
        )
    }
}
