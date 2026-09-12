package com.example.agent.nexus.io

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelContractTest {
    @Test
    fun inputChannelProducesUnifiedMessage() = runBlocking {
        val channel = FakeInputChannel(AgentMessage("hello", "text"))

        assertEquals("hello", channel.receive()?.content)
        assertEquals("text", channel.receive()?.sourceId)
    }

    @Test
    fun outputChannelReceivesUnifiedResponse() = runBlocking {
        val channel = FakeOutputChannel()

        channel.send(AgentResponse("hello"))

        assertEquals("hello", channel.lastResponse?.content)
    }

    @Test
    fun outputChannelCanAggregateStreamingChunks() = runBlocking {
        val channel = FakeOutputChannel()

        channel.sendStream(flowOf("Hel", "lo"))

        assertEquals("Hello", channel.lastResponse?.content)
    }

    private class FakeInputChannel(
        private val message: AgentMessage?
    ) : InputChannel {
        override val id: String = "fake-input"
        override suspend fun receive(): AgentMessage? = message
    }

    private class FakeOutputChannel : OutputChannel {
        override val id: String = "fake-output"
        var lastResponse: AgentResponse? = null

        override suspend fun send(response: AgentResponse) {
            lastResponse = response
        }
    }
}
