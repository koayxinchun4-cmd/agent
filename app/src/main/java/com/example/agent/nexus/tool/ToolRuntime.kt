package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Result of the policy layer before an underlying AgentTool runs. */
sealed interface ToolRuntimeResult {
    data class Success(val result: ToolResult) : ToolRuntimeResult
    data class Denied(val message: String) : ToolRuntimeResult
}

/** Common runtime boundary for cancellation, permissions and confirmation. */
interface ToolRuntime {
    suspend fun execute(
        sessionId: String,
        task: AgentTask,
        tool: AgentTool,
        confirmationGranted: Boolean = false,
        grantedPermissions: Set<String> = emptySet()
    ): ToolRuntimeResult
}

class DefaultToolRuntime(
    private val approvals: ToolApprovalStore = InMemoryToolApprovalStore()
) : ToolRuntime {
    override suspend fun execute(
        sessionId: String,
        task: AgentTask,
        tool: AgentTool,
        confirmationGranted: Boolean,
        grantedPermissions: Set<String>
    ): ToolRuntimeResult {
        currentCoroutineContext().ensureActive()

        val effectivePermissions = if (grantedPermissions.isEmpty()) {
            task.metadata[AgentTask.GRANTED_PERMISSIONS]
                ?.split(',')
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.toSet()
                .orEmpty()
        } else {
            grantedPermissions
        }

        if (!effectivePermissions.containsAll(tool.requiredPermissions)) {
            val missing = tool.requiredPermissions - effectivePermissions
            return ToolRuntimeResult.Denied(
                "Missing required Android permission(s): ${missing.sorted().joinToString()}"
            )
        }

        val effectiveConfirmation = confirmationGranted ||
            task.metadata[AgentTask.CONFIRMATION_GRANTED] == "true"
        if (tool.riskLevel == RiskLevel.REQUIRES_CONFIRMATION) {
            val approved = approvals.get(sessionId, tool.id)
            if (approved != ApprovalDecision.APPROVED && !effectiveConfirmation) {
                return ToolRuntimeResult.Denied(
                    "User confirmation is required before executing ${tool.name}"
                )
            }
            if (effectiveConfirmation) {
                approvals.put(sessionId, tool.id, ApprovalDecision.APPROVED, ApprovalScope.SESSION)
            }
        }

        currentCoroutineContext().ensureActive()
        return runCatching { tool.execute(task) }
            .fold(
                onSuccess = { ToolRuntimeResult.Success(it) },
                onFailure = { error ->
                    ToolRuntimeResult.Success(
                        ToolResult.Failure(
                            "Tool execution failed: ${error.message ?: "unknown error"}",
                            error
                        )
                    )
                }
            )
    }
}
