package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import java.io.File

/**
 * Safe Office workspace primitive. It works only inside the supplied private
 * workspace and currently supports text-oriented Office inputs (CSV/TSV/TXT/MD)
 * plus metadata inspection for common .docx/.xlsx/.pptx files.
 */
class OfficeTool(
    private val workspace: File
) : AgentTool {
    override val id: String = "office"
    override val name: String = "Office Agent"
    override val description: String = "Inspects and reads supported office workspace files safely."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("Office 任務內容不能為空")

        return runCatching {
            val requested = extractPath(input)
            if (requested == null) {
                listFiles()
            } else {
                inspectFile(requested)
            }
        }.getOrElse { ToolResult.Failure("Office 工具执行失败：${it.message ?: "未知错误"}", it) }
    }

    private fun listFiles(): ToolResult {
        val files = workspace.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in SUPPORTED_EXTENSIONS }
            .take(MAX_LIST_ENTRIES)
            .map { it.relativeTo(workspace).path }
            .toList()
        val body = if (files.isEmpty()) "Office workspace 目前没有可处理的文件。" else files.joinToString("\n")
        return ToolResult.Success("Office 文件列表\n$body")
    }

    private fun inspectFile(relativePath: String): ToolResult {
        val target = File(workspace, relativePath).canonicalFile
        val root = workspace.canonicalFile
        if (target != root && !target.path.startsWith(root.path + File.separator)) {
            return ToolResult.Failure("禁止访问 Office workspace 之外的文件")
        }
        if (!target.isFile) return ToolResult.Failure("找不到 Office 文件：$relativePath")
        if (target.extension.lowercase() !in SUPPORTED_EXTENSIONS) {
            return ToolResult.Failure("不支持的 Office 文件类型：.${target.extension}")
        }
        if (target.length() > MAX_READ_BYTES) return ToolResult.Failure("文件过大，暂不读取")

        val extension = target.extension.lowercase()
        val text = if (extension in TEXT_EXTENSIONS) {
            target.readText(Charsets.UTF_8).take(MAX_READ_CHARS)
        } else {
            "此文件类型支持安全元数据检查；二进制 Office 内容解析尚未启用。"
        }
        return ToolResult.Success(
            "Office 文件检查完成\n文件：$relativePath\n类型：.$extension\n大小：${target.length()} bytes\n\n$text"
        )
    }

    private fun extractPath(input: String): String? {
        val marker = Regex("(?:读取|检查|read|inspect)\\s+(.+)", RegexOption.IGNORE_CASE)
            .find(input) ?: return null
        return marker.groupValues[1].trim().ifEmpty { null }
    }

    private companion object {
        val TEXT_EXTENSIONS = setOf("csv", "tsv", "txt", "md")
        val SUPPORTED_EXTENSIONS = TEXT_EXTENSIONS + setOf("docx", "xlsx", "pptx")
        const val MAX_LIST_ENTRIES = 100
        const val MAX_READ_BYTES = 512 * 1024L
        const val MAX_READ_CHARS = 60_000
    }
}
