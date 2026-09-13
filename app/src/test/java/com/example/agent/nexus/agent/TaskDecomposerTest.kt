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

    @Test
    fun `splits task with chinese conjunction connectors`() {
        assertEquals(
            listOf("搜尋資料", "整理報告"),
            decomposer.decompose("搜尋資料以及整理報告")
        )
    }

    @Test
    fun `splits task with chinese hai-you connector`() {
        assertEquals(
            listOf("檢查 CI", "看 PR"),
            decomposer.decompose("檢查 CI還有看 PR")
        )
    }

    @Test
    fun `splits malay sequential task with dan`() {
        assertEquals(
            listOf("cari maklumat", "ringkaskan hasil"),
            decomposer.decompose("cari maklumat dan ringkaskan hasil")
        )
    }

    @Test
    fun `splits malay sequential task with kemudian`() {
        assertEquals(
            listOf("buka fail", "semak kandungan"),
            decomposer.decompose("buka fail kemudian semak kandungan")
        )
    }

    @Test
    fun `splits malay sequential task dengan serta`() {
        assertEquals(
            listOf("cari repo", "baca isu"),
            decomposer.decompose("cari repo serta baca isu")
        )
    }

    @Test
    fun `splits english task with and also`() {
        assertEquals(
            listOf("search the repo", "check the build"),
            decomposer.decompose("search the repo and also check the build")
        )
    }

    @Test
    fun `removes english ordinal list markers`() {
        assertEquals(
            listOf("search the repo", "read the issue", "summarize"),
            decomposer.decompose("first, search the repo\nsecond, read the issue\nthird, summarize")
        )
    }
}
