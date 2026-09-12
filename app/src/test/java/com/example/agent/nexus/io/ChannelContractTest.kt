package com.example.agent.nexus.io

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        assertEquals("hello", channel.responses.single().content)
    }

    @Test
    fun outputChannelForwardsStreamingChunksIncrementally() = runBlocking {
        val channel = FakeOutputChannel()

        channel.sendStream(flowOf("Hel", "lo"))

        assertEquals(listOf("Hel", "lo"), channel.responses.map { it.content })
    }

    @Test
    fun defaultStreamingContractUsesSendForEachChunk() = runBlocking {
        val channel = FakeOutputChannel()

        channel.sendStream(flowOf("one", "two", "three"))

        assertEquals(listOf("one", "two", "three"), channel.responses.map { it.content })
    }

    @Test
    fun textOutputChannelForwardsChunksAsTheyArrive() = runBlocking {
        val received = mutableListOf<String>()
        val channel = TextOutputChannel { response -> received += response.content }

        channel.sendStream(flowOf("A", "B", "C"))

        assertEquals(listOf("A", "B", "C"), received)
    }

    private class FakeInputChannel(
        private val message: AgentMessage?
    ) : InputChannel {
        override val id: String = "fake-input"
        override suspend fun receive(): AgentMessage? = message
    }

    private class FakeOutputChannel : OutputChannel {
        override val id: String = "fake-output"
        val responses = mutableListOf<AgentResponse>()

        override suspend fun send(response: AgentResponse) {
            responses += response
        }
    }
}
