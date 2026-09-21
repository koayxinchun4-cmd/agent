package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolResult

/** Result of checking whether a tool execution is usable for the current task. */
sealed interface VerificationResult {
    data object Passed : VerificationResult
    data class Retry(val reason: String) : VerificationResult
}

/**
 * Deterministic v1 verifier.
 *
 * It does not claim that a tool output is semantically correct. It only checks
 * the minimum contract needed by the execution loop: success must contain
 * usable text, while failures may be retried.
 */
class AgentVerifier {
    fun verify(result: ToolResult): VerificationResult = when (result) {
        is ToolResult.Success -> if (result.text.isNotBlank()) {
            VerificationResult.Passed
        } else {
            VerificationResult.Retry("工具返回了空结果")
        }

        is ToolResult.Failure -> VerificationResult.Retry(
            result.message.ifBlank { "工具执行失败" }
        )
    }
}
