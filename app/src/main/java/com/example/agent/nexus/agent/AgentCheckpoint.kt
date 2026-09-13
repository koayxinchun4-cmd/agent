package com.example.agent.nexus.agent

/** Persistence boundary for resumable Agent execution. */
interface AgentCheckpointStore {
    fun save(session: AgentSession, turn: AgentTurn)
    fun load(sessionId: String): AgentCheckpoint?
    fun clear(sessionId: String)
}

data class AgentCheckpoint(
    val session: AgentSession,
    val turn: AgentTurn
)

/** Lightweight implementation for process-local recovery and tests. */
class InMemoryAgentCheckpointStore : AgentCheckpointStore {
    private val checkpoints = mutableMapOf<String, AgentCheckpoint>()

    override fun save(session: AgentSession, turn: AgentTurn) {
        checkpoints[session.id] = AgentCheckpoint(session, turn)
    }

    override fun load(sessionId: String): AgentCheckpoint? = checkpoints[sessionId]

    override fun clear(sessionId: String) {
        checkpoints.remove(sessionId)
    }
}
