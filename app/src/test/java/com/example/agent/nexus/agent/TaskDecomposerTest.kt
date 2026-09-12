package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDecomposerTest {
    private val decomposer = TaskDecomposer()

    @Test
    fun `keeps simple request as one subtask`() {
        assertEquals(listOf("搜尋 Nexus"), decomposer.decompose("搜尋 Nexus"))
    }

    @Test
    fun `splits english sequential task`() {
        assertEquals(
            listOf("search the repo", "read the issue", "summarize it"),
            decomposer.decompose("search the repo then read the issue then summarize it")
        )
    }

    @Test
    fun `splits traditional chinese sequential task`() {
        assertEquals(
            listOf("搜尋 repo", "讀取 issue", "整理結果"),
            decomposer.decompose("搜尋 repo，讀取 issue，整理結果")
        )
    }

    @Test
    fun `removes numbered list markers`() {
        assertEquals(
            listOf("搜尋 repo", "檢查 CI", "整理結果"),
            decomposer.decompose("1. 搜尋 repo\n2. 檢查 CI\n3. 整理結果")
        )
    }

    @Test
    fun `blank input produces no subtasks`() {
        assertTrue(decomposer.decompose("   ").isEmpty())
    }
}
