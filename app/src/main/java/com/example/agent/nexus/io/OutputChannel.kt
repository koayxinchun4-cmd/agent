package com.example.agent.nexus.io

import kotlinx.coroutines.flow.Flow

/**
 * Delivers channel-neutral agent responses to an external destination.
 * Implementations decide how full responses or incremental chunks are rendered.
 */
interface OutputChannel {
    val id: String

    suspend fun send(response: AgentResponse)

    suspend fun sendStream(chunks: Flow<String>) {
        val content = buildString {
            chunks.collect { append(it) }
        }
        send(AgentResponse(content = content))
    }
}
