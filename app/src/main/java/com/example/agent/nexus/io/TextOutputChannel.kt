package com.example.agent.nexus.io

/**
 * Adapter for text-oriented consumers.
 * The sink is injected so the contract stays independent of Compose and Android UI types.
 */
class TextOutputChannel(
    override val id: String = "text",
    private val sink: suspend (AgentResponse) -> Unit
) : OutputChannel {
    override suspend fun send(response: AgentResponse) {
        sink(response)
    }
}
