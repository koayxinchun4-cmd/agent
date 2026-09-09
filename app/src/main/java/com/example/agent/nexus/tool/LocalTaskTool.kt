package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/**
 * Deterministic on-device tool used as the first real executable capability.
 * It does not call a network service and is safe to use as a runtime smoke path.
 */
class LocalTaskTool : AgentTool {
    override val id: String = "local_task"
    override val name: String = "Local Task Analyzer"
    override val description: String = "Analyzes a task locally and returns an execution-ready summary."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) {
            return ToolResult.Failure("任務內容不能為空")
        }

        val category = when {
            input.contains("android", ignoreCase = true) || input.contains("安卓") -> "Android"
            input.contains("github", ignoreCase = true) -> "GitHub"
            input.contains("代碼") || input.contains("code", ignoreCase = true) -> "Coding"
            input.contains("文件") || input.contains("file", ignoreCase = true) -> "Files"
            input.contains("網頁") || input.contains("web", ignoreCase = true) || input.contains("搜索") -> "Research"
            else -> "General"
        }

        return ToolResult.Success(
            "本地任務分析完成\n" +
                "目標：$input\n" +
                "分類：$category\n" +
                "執行方式：Nexus Local Tool\n" +
                "下一步：等待對應專業 Tool 接入後繼續執行。"
        )
    }
}
