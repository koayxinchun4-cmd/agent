package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class LocalFileToolTest {
    @Test
    fun listsFilesInsidePrivateRoot() = runBlocking {
        val root = Files.createTempDirectory("nexus-file-tool").toFile()
        root.resolve("note.txt").writeText("hello")

        val result = LocalFileTool(root).execute(
            AgentTask("test", "列出文件")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("note.txt"))
    }

    @Test
    fun readsFileInsidePrivateRoot() = runBlocking {
        val root = Files.createTempDirectory("nexus-file-tool").toFile()
        root.resolve("note.txt").writeText("hello nexus")

        val result = LocalFileTool(root).execute(
            AgentTask("test", "读取 note.txt")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("hello nexus"))
    }

    @Test
    fun readsFileWithEnglishRequest() = runBlocking {
        val root = Files.createTempDirectory("nexus-file-tool").toFile()
        root.resolve("note.txt").writeText("hello nexus")

        val result = LocalFileTool(root).execute(
            AgentTask("test", "read note.txt")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("hello nexus"))
    }

    @Test
    fun blocksPathTraversal() = runBlocking {
        val root = Files.createTempDirectory("nexus-file-tool").toFile()
        val outside = root.parentFile.resolve("outside.txt")
        outside.writeText("secret")

        val result = LocalFileTool(root).execute(
            AgentTask("test", "读取 ../${outside.name}")
        )

        assertTrue(result is ToolResult.Failure)
    }
}
