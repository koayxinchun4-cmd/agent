package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask

/** A capability that Nexus can invoke for a task. */
interface AgentTool {
    val id: String
    val name: String
    val description: String

    suspend fun execute(task: AgentTask): ToolResult
}

sealed interface ToolResult {
    data class Success(val text: String) : ToolResult
    data class Failure(val message: String, val cause: Throwable? = null) : ToolResult
}
