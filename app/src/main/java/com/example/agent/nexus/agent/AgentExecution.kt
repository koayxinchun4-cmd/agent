package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolResult

/** A single observable step in an Agent run. */
data class AgentStepResult(
    val step: String,
    val success: Boolean,
    val output: String
)

/** Execution result with the plan, attempts and tool steps kept separate for UI/logging. */
data class AgentExecution(
    val plan: AgentPlan,
    val steps: List<AgentStepResult>,
    val result: AgentResult,
    val attempts: Int = 1
)

internal fun ToolResult.toAgentStep(step: String): AgentStepResult = when (this) {
    is ToolResult.Success -> AgentStepResult(step, true, text)
    is ToolResult.Failure -> AgentStepResult(step, false, message)
}
