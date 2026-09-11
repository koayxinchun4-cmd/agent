package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** Central registry for Nexus Agent capabilities. */
class ToolRegistry(tools: List<AgentTool> = emptyList()) {
    private val toolsById = tools.associateBy { it.id }.toMutableMap()

    fun register(tool: AgentTool) {
        toolsById[tool.id] = tool
    }

    fun get(toolId: String): AgentTool? = toolsById[toolId]

    fun list(): List<AgentTool> = toolsById.values.toList()

    suspend fun execute(toolId: String, task: AgentTask): ToolResult {
        val tool = get(toolId)
            ?: return ToolResult.Failure("Nexus 找不到工具：$toolId")
        if (tool.riskLevel == RiskLevel.REQUIRES_CONFIRMATION &&
            task.metadata[AgentTask.CONFIRMATION_GRANTED] != "true"
        ) {
            return ToolResult.Failure("工具 $toolId 需要明确用户确认后才能执行")
        }
        return runCatching { tool.execute(task) }
            .getOrElse { ToolResult.Failure("工具执行失败：${it.message ?: "未知错误"}", it) }
    }
}
