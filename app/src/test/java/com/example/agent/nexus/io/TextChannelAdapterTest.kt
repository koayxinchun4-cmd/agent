package com.example.agent.nexus.io

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextChannelAdapterTest {
    @Test
    fun textInputAdaptsProducerIntoAgentMessage() = runBlocking {
        val channel = TextInputChannel(
            id = "text-input",
            source = { "hello" },
            metadata = mapOf("origin" to "compose")
        )

        val message = channel.receive()

        assertEquals("hello", message?.content)
        assertEquals("text-input", message?.sourceId)
        assertEquals("compose", message?.metadata?.get("origin"))
    }

    @Test
    fun textInputReturnsNullWhenProducerHasNoMessage() = runBlocking {
        val channel = TextInputChannel(source = { null })

        assertNull(channel.receive())
    }

    @Test
    fun textOutputAdaptsAgentResponseIntoConsumerSink() = runBlocking {
        var received: AgentResponse? = null
        val channel = TextOutputChannel(
            id = "text-output",
            sink = { response -> received = response }
        )

        channel.send(AgentResponse(content = "hello", metadata = mapOf("task" to "1")))

        assertEquals("hello", received?.content)
        assertEquals("1", received?.metadata?.get("task"))
    }
}
