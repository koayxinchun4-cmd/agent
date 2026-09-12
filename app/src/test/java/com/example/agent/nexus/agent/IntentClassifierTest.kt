package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentClassifierTest {
    private val classifier = IntentClassifier()

    @Test
    fun `classifies app launch intent`() {
        assertEquals(AgentIntent.App, classifier.classify("open app package:com.example.notes"))
    }

    @Test
    fun `classifies github intent`() {
        assertEquals(AgentIntent.GitHub, classifier.classify("check GitHub CI"))
    }

    @Test
    fun `classifies traditional chinese web intent`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("搜尋這個主題的資料"))
    }

    @Test
    fun `classifies simplified chinese web intent`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("搜索這個主題的資料"))
    }

    @Test
    fun `classifies traditional chinese file intent`() {
        assertEquals(AgentIntent.File, classifier.classify("讀取文件 note.txt"))
    }

    @Test
    fun `classifies traditional chinese memory intent`() {
        assertEquals(AgentIntent.Memory, classifier.classify("記住這個偏好"))
    }

    @Test
    fun `classifies general intent when no specialized signal exists`() {
        assertEquals(AgentIntent.General, classifier.classify("hello Nexus"))
    }

    @Test
    fun `classifies blank input as general`() {
        assertEquals(AgentIntent.General, classifier.classify("   "))
    }
}
