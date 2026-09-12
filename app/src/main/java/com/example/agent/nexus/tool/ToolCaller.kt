package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** Executes a planned tool call through the central ToolRegistry. */
class ToolCaller(private val registry: ToolRegistry) {
    suspend fun execute(call: ToolCall, task: AgentTask): ToolResult {
        require(call.taskId == task.id) { "ToolCall taskId must match AgentTask id" }
        return registry.execute(call.toolId, task)
    }
}
