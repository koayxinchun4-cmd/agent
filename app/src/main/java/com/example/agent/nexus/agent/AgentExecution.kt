package com.example.agent.nexus.agent

import com.example.agent.nexus.memory.AgentExecutionContext
import com.example.agent.nexus.tool.ToolResult

data class AgentExecution(
    val plan: AgentPlan,
    val steps: List<AgentStepResult>,
    val result: AgentResult,
    val attempts: Int = 1,
    val context: AgentExecutionContext? = null
)

internal fun ToolResult.toAgentStep(step: String): AgentStepResult = when (this) {
    is ToolResult.Success -> AgentStepResult(step, true, text)
    is ToolResult.Failure -> AgentStepResult(step, false, message)
}