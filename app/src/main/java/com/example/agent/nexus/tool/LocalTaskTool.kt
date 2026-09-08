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
            return ToolResult.Failure("任务内容不能为空")
        }

        val category = when {
            input.contains("android", ignoreCase = true) || input.contains("安卓") -> "Android"
            input.contains("github", ignoreCase = true) -> "GitHub"
            input.contains("代码") || input.contains("code", ignoreCase = true) -> "Coding"
            input.contains("文件") || input.contains("file", ignoreCase = true) -> "Files"
            input.contains("网页") || input.contains("web", ignoreCase = true) || input.contains("搜索") -> "Research"
            else -> "General"
        }

        return ToolResult.Success(
            "本地任务分析完成\n" +
                "目标：$input\n" +
                "分类：$category\n" +
                "执行方式：Nexus Local Tool\n" +
                "下一步：等待对应专业 Tool 接入后继续执行。"
        )
    }
}
