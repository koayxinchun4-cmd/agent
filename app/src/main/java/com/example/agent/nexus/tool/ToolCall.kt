package com.example.agent.nexus.tool

/**
 * Stable contract for one planned tool invocation.
 * Execution remains owned by ToolRegistry; this value only describes the call.
 */
data class ToolCall(
    val taskId: String,
    val toolId: String,
    val stepIndex: Int = 0
) {
    init {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
        require(toolId.isNotBlank()) { "toolId must not be blank" }
        require(stepIndex >= 0) { "stepIndex must be >= 0" }
    }
}
