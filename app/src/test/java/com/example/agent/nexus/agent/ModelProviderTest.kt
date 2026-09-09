package com.example.agent.nexus.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelProviderTest {
    private val provider = LocalModelProvider()

    @Test
    fun `local provider is always available`() {
        assertTrue(provider.isAvailable)
        assertEquals(ModelRoute.Local, provider.route)
    }

    @Test
    fun `local provider returns actionable response`() = runBlocking {
        val response = provider.generate("create a task")

        assertEquals("local", response.providerId)
        assertEquals(ModelRoute.Local, response.route)
        assertTrue(response.text.contains("create a task"))
    }

    @Test
    fun `registry exposes available local route`() {
        val registry = ModelProviderRegistry(listOf(provider))

        assertEquals(provider, registry.get(ModelRoute.Local))
        assertEquals(setOf(ModelRoute.Local), registry.availableRoutes())
    }
}
