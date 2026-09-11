package com.example.agent.nexus.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiModelProviderTest {
    @Test
    fun recognizesConfiguredApiKey() {
        assertTrue(GeminiModelProvider.isConfigured("test-key"))
    }

    @Test
    fun rejectsBlankApiKey() {
        assertFalse(GeminiModelProvider.isConfigured("   "))
    }

    @Test
    fun rejectsExamplePlaceholder() {
        assertFalse(GeminiModelProvider.isConfigured("MY_GEMINI_API_KEY"))
    }
}
