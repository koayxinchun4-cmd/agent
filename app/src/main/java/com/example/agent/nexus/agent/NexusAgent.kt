package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolRegistry

/**
 * Minimal orchestration layer. The first implementation intentionally keeps
 * planning simple so existing chat/model integrations remain untouched.
 */
class NexusAgent(
    private val toolRegistry: ToolRegistry
) {
    suspend fun execute(task: AgentTask, toolId: String? = null): AgentResult {
        if (toolId == null) {
            return AgentResult.Success("任务已接收：${task.input}")
        }

        return when (val result = toolRegistry.execute(toolId, task)) {
            is com.example.agent.nexus.tool.ToolResult.Success ->
                AgentResult.Success(result.text)
            is com.example.agent.nexus.tool.ToolResult.Failure ->
                AgentResult.Failure(result.message, result.cause)
        }
    }
}
