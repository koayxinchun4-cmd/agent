package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** Central policy for deciding whether a tool may execute for a task. */
object ToolPermission {
    fun check(tool: AgentTool, task: AgentTask): ToolResult.Failure? {
        if (tool.riskLevel == RiskLevel.REQUIRES_CONFIRMATION &&
            task.metadata[AgentTask.CONFIRMATION_GRANTED] != "true"
        ) {
            return ToolResult.Failure("工具 ${tool.id} 需要明确用户确认后才能执行")
        }
        return null
    }
}
