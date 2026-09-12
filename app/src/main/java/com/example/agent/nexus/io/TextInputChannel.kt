package com.example.agent.nexus.io

/**
 * Adapter for text-oriented producers.
 * The producer is injected so this contract stays independent of Compose and Android UI types.
 */
class TextInputChannel(
    override val id: String = "text",
    private val source: suspend () -> String?,
    private val metadata: Map<String, String> = emptyMap()
) : InputChannel {
    override suspend fun receive(): AgentMessage? =
        source()?.let { content ->
            AgentMessage(
                content = content,
                sourceId = id,
                metadata = metadata
            )
        }
}
