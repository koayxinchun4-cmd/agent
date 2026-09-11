package com.example.agent.nexus.tool

import java.io.ByteArrayInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

class SafFileReaderTest {
    @Test
    fun readsSelectedTextWithinBound() {
        val reader = SafFileReader { ByteArrayInputStream("hello nexus".toByteArray()) }

        val result = reader.read("content://selected/file")

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("hello nexus"))
    }

    @Test
    fun rejectsSelectedFileOverBound() {
        val reader = SafFileReader { ByteArrayInputStream(ByteArray(9)) }

        val result = reader.read("content://selected/file", maxBytes = 8)

        assertTrue(result is ToolResult.Failure)
        assertTrue((result as ToolResult.Failure).message.contains("过大"))
    }

    @Test
    fun rejectsMissingUri() {
        val reader = SafFileReader { ByteArrayInputStream("unused".toByteArray()) }

        val result = reader.read("  ")

        assertTrue(result is ToolResult.Failure)
    }
}
