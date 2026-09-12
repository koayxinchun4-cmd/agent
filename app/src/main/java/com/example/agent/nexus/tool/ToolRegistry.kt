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
        ToolPermission.check(tool, task)?.let { return it }
        return runCatching { tool.execute(task) }
            .getOrElse { ToolResult.Failure("Tool execution failed: ${it.message ?: "unknown error"}", it) }
    }
}
