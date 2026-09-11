package com.example.agent.nexus.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenRouterModelProviderTest {
    @Test
    fun recognizesConfiguredApiKey() {
        assertTrue(OpenRouterModelProvider.isConfigured("test-key"))
    }

    @Test
    fun rejectsBlankApiKey() {
        assertFalse(OpenRouterModelProvider.isConfigured("   "))
    }

    @Test
    fun rejectsExamplePlaceholder() {
        assertFalse(OpenRouterModelProvider.isConfigured("MY_OPENROUTER_API_KEY"))
    }

    @Test
    fun rejectsBlankModel() {
        assertFalse(OpenRouterModelProvider.isConfigured("test-key", "   "))
    }

    @Test
    fun usesAutomaticRoutingByDefault() {
        assertTrue(
            OpenRouterModelProvider.isConfigured(
                "test-key",
                OpenRouterModelProvider.DEFAULT_MODEL
            )
        )
    }
}
