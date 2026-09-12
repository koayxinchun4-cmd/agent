package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** Central policy for deciding whether a tool may execute for a task. */
object ToolPermission {
    fun check(tool: AgentTool, task: AgentTask): ToolResult.Failure? {
        if (tool.riskLevel == RiskLevel.REQUIRES_CONFIRMATION &&
            task.metadata[AgentTask.CONFIRMATION_GRANTED] != "true"
        ) {
            return ToolResult.Failure("Tool ${tool.id} requires explicit user confirmation")
        }

        val grantedPermissions = task.metadata[AgentTask.GRANTED_PERMISSIONS]
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.toSet()
            .orEmpty()
        val missingPermissions = tool.requiredPermissions - grantedPermissions

        if (missingPermissions.isNotEmpty()) {
            return ToolResult.Failure(
                "Tool ${tool.id} requires Android permission(s): ${missingPermissions.joinToString()}. " +
                    "Grant the permission before retrying."
            )
        }

        return null
    }
}
