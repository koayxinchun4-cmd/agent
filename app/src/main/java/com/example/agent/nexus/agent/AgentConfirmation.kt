package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.AgentTool

/** A pre-execution confirmation request for a tool that requires user approval. */
data class AgentConfirmationRequest(
    val task: AgentTask,
    val tool: AgentTool,
    val plan: AgentPlan
)
