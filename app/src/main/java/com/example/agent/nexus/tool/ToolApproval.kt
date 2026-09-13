package com.example.agent.nexus.tool

/** Approval scope for a tool capability. */
enum class ApprovalScope {
    ONCE,
    SESSION
}

/** User decision made before a risky tool is executed. */
enum class ApprovalDecision {
    APPROVED,
    DENIED
}

/** Session-scoped approval cache, intentionally separate from tool implementation. */
interface ToolApprovalStore {
    fun get(sessionId: String, toolId: String): ApprovalDecision?
    fun put(sessionId: String, toolId: String, decision: ApprovalDecision, scope: ApprovalScope)
}

class InMemoryToolApprovalStore : ToolApprovalStore {
    private val sessionApprovals = mutableMapOf<Pair<String, String>, ApprovalDecision>()

    override fun get(sessionId: String, toolId: String): ApprovalDecision? =
        sessionApprovals[sessionId to toolId]

    override fun put(
        sessionId: String,
        toolId: String,
        decision: ApprovalDecision,
        scope: ApprovalScope
    ) {
        if (scope == ApprovalScope.SESSION) {
            sessionApprovals[sessionId to toolId] = decision
        }
    }
}
