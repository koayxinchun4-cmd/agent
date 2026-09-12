package com.example.agent.nexus.agent

/** A user goal that Nexus can plan and execute through registered tools. */
data class AgentTask(
    val id: String,
    val input: String,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        const val CONFIRMATION_GRANTED = "confirmation_granted"
        const val GRANTED_PERMISSIONS = "granted_permissions"
        const val PROJECT_ID = "project_id"
    }
}
