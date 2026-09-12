package com.example.agent.nexus.io

import kotlinx.coroutines.flow.Flow

/**
 * Delivers channel-neutral agent responses to an external destination.
 * Implementations decide how full responses or incremental chunks are rendered.
 */
interface OutputChannel {
    val id: String

    suspend fun send(response: AgentResponse)

    /** Delivers one generated chunk without waiting for the remaining chunks. */
    suspend fun sendChunk(chunk: String) {
        send(AgentResponse(content = chunk))
    }

    /** Forwards generated chunks incrementally as they arrive. */
    suspend fun sendStream(chunks: Flow<String>) {
        chunks.collect { chunk -> sendChunk(chunk) }
    }
}
