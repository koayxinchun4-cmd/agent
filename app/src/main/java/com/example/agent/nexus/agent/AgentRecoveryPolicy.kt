package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.ToolResult

/** Diagnoses whether a failed execution is safe to retry automatically. */
sealed interface FailureDiagnosis {
    data class Transient(val reason: String) : FailureDiagnosis
    data class Permanent(val reason: String) : FailureDiagnosis
}

/** Small, deterministic recovery policy for Agent Loop v1. */
class AgentRecoveryPolicy {
    fun diagnose(result: ToolResult): FailureDiagnosis = when (result) {
        is ToolResult.Success -> FailureDiagnosis.Permanent("No failure to recover")
        is ToolResult.Failure -> diagnoseMessage(result.message)
    }

    fun diagnoseVerification(reason: String): FailureDiagnosis =
        FailureDiagnosis.Transient("Verifier rejected the result: ${reason.ifBlank { "unknown reason" }}")

    fun adjust(task: AgentTask, diagnosis: FailureDiagnosis, attempt: Int): AgentTask {
        val reason = when (diagnosis) {
            is FailureDiagnosis.Transient -> diagnosis.reason
            is FailureDiagnosis.Permanent -> diagnosis.reason
        }
        return task.copy(
            input = task.input + "\n[Recovery attempt $attempt: $reason; use a safe alternative if possible.]",
            metadata = task.metadata + (RECOVERY_ATTEMPT_KEY to attempt.toString())
        )
    }

    private fun diagnoseMessage(message: String): FailureDiagnosis {
        val normalized = message.lowercase()
        val transient = listOf(
            "timeout", "timed out", "temporarily", "temporary", "unavailable",
            "connection", "network", "http 408", "http 429", "http 500", "http 502",
            "http 503", "http 504", "try again", "暫時", "暂时", "逾時", "超時", "逾时", "超时",
            "網路", "网络", "連線", "连接"
        ).any(normalized::contains)
        return if (transient) {
            FailureDiagnosis.Transient(message.ifBlank { "Transient tool failure" })
        } else {
            FailureDiagnosis.Permanent(message.ifBlank { "Permanent tool failure" })
        }
    }

    private companion object {
        const val RECOVERY_ATTEMPT_KEY = "recovery_attempt"
    }
}
