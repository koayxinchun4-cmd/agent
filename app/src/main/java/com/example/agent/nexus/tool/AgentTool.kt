package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** Risk level for a capability. Risky or irreversible tools require explicit user confirmation. */
enum class RiskLevel {
    SAFE,
    REQUIRES_CONFIRMATION
}

/** A capability that Nexus can invoke for a task. */
interface AgentTool {
    val id: String
    val name: String
    val description: String
    val riskLevel: RiskLevel
        get() = RiskLevel.SAFE

    /** Android runtime permissions that must already be granted before execution. */
    val requiredPermissions: Set<String>
        get() = emptySet()

    suspend fun execute(task: AgentTask): ToolResult
}

sealed interface ToolResult {
    data class Success(val text: String) : ToolResult
    data class Failure(val message: String, val cause: Throwable? = null) : ToolResult
}
