package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/**
 * Deterministic on-device task tool. It produces an actionable execution
 * summary without requiring a backend or network service.
 */
class LocalTaskTool : AgentTool {
    override val id: String = "local_task"
    override val name: String = "Local Task Analyzer"
    override val description: String = "Analyzes a task locally and returns an actionable execution summary."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("任务内容不能为空")

        val category = classify(input)
        val actions = suggestedActions(category)

        return ToolResult.Success(
            buildString {
                appendLine("本地任务分析完成")
                appendLine("目标：$input")
                appendLine("分类：$category")
                appendLine("执行方式：Nexus Local Tool")
                appendLine("建议执行路径：")
                actions.forEachIndexed { index, action ->
                    appendLine("${index + 1}. $action")
                }
                append("当前能力边界：本地分析已完成；需要网络、GitHub、系统或更专业的 Tool 时，Nexus 应切换到对应能力，而不是假装已经执行。")
            }
        )
    }

    private fun classify(input: String): String = when {
        input.contains("android", ignoreCase = true) || input.contains("安卓") -> "Android"
        input.contains("github", ignoreCase = true) -> "GitHub"
        input.contains("代码") || input.contains("code", ignoreCase = true) -> "Coding"
        input.contains("文件") || input.contains("file", ignoreCase = true) -> "Files"
        input.contains("网页") || input.contains("web", ignoreCase = true) || input.contains("搜索") -> "Research"
        else -> "General"
    }

    private fun suggestedActions(category: String): List<String> = when (category) {
        "Android" -> listOf("检查 Android / Gradle / Compose 上下文", "定位问题或目标", "运行可用验证", "汇总结果")
        "GitHub" -> listOf("识别仓库或 GitHub 资源", "读取允许访问的内容", "分析变更或问题", "汇总结果")
        "Coding" -> listOf("理解代码目标", "定位相关文件", "分析实现路径", "验证修改结果")
        "Files" -> listOf("定位目标文件", "读取允许访问的内容", "分析文件内容", "汇总结果")
        "Research" -> listOf("明确研究问题", "收集可信来源", "交叉检查结果", "汇总结论")
        else -> listOf("理解目标", "拆分可执行步骤", "选择合适 Tool", "验证并汇总结果")
    }
}
