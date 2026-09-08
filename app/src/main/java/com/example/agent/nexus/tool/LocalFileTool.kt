package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import java.io.File

/**
 * Safe first File capability. It is intentionally limited to Nexus's private
 * app storage; broader phone-file access will later use Android's Storage
 * Access Framework with explicit user permission.
 */
class LocalFileTool(
    private val rootDirectory: File
) : AgentTool {
    override val id: String = "file_agent"
    override val name: String = "Local File Agent"
    override val description: String = "Lists or reads files inside Nexus private app storage."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("文件任务内容不能为空")

        return runCatching {
            when {
                input.contains("读取") || input.contains("read", ignoreCase = true) ->
                    readRequestedFile(input)
                else -> listFiles()
            }
        }.getOrElse { ToolResult.Failure("文件工具执行失败：${it.message ?: "未知错误"}", it) }
    }

    private fun listFiles(): ToolResult {
        val entries = rootDirectory.walkTopDown()
            .filter { it.isFile }
            .take(MAX_LIST_ENTRIES)
            .map { it.relativeTo(rootDirectory).path }
            .toList()

        val body = if (entries.isEmpty()) {
            "Nexus 私有存储目前没有文件。"
        } else {
            entries.joinToString("\n")
        }

        return ToolResult.Success(
            "Nexus 私有文件列表\n$body"
        )
    }

    private fun readRequestedFile(input: String): ToolResult {
        val marker = input.indexOf(READ_MARKER)
        if (marker < 0) {
            return ToolResult.Failure("请提供要读取的相对文件路径")
        }

        val relativePath = input.substring(marker + READ_MARKER.length).trim()
        if (relativePath.isEmpty()) {
            return ToolResult.Failure("请提供要读取的相对文件路径")
        }

        val target = File(rootDirectory, relativePath).canonicalFile
        val root = rootDirectory.canonicalFile
        if (target != root && !target.path.startsWith(root.path + File.separator)) {
            return ToolResult.Failure("禁止访问 Nexus 私有目录之外的文件")
        }
        if (!target.isFile) {
            return ToolResult.Failure("找不到文件：$relativePath")
        }
        if (target.length() > MAX_READ_BYTES) {
            return ToolResult.Failure("文件过大，暂不读取（上限 ${MAX_READ_BYTES / 1024} KB）")
        }

        return ToolResult.Success(
            "文件读取完成：$relativePath\n\n${target.readText(Charsets.UTF_8)}"
        )
    }

    private companion object {
        const val READ_MARKER = "读取"
        const val MAX_LIST_ENTRIES = 100
        const val MAX_READ_BYTES = 256 * 1024L
    }
}
