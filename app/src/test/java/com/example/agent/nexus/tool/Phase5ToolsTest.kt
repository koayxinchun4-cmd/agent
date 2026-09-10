package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.nio.file.Files

class Phase5ToolsTest {
    @Test
    fun webResearchRejectsMissingUrl() = runBlocking {
        val result = WebResearchTool().execute(AgentTask("web", "搜尋 Nexus"))
        assertTrue(result is ToolResult.Failure)
    }

    @Test
    fun webResearchFetchesInjectedResponse() = runBlocking {
        val result = WebResearchTool { FakeConnection("https://example.com") }
            .execute(AgentTask("web", "研究 https://example.com"))
        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("fake web response"))
    }

    @Test
    fun officeReadsCsvInsideWorkspace() = runBlocking {
        val root = Files.createTempDirectory("nexus-office-tool").toFile()
        root.resolve("budget.csv").writeText("item,total\ncoffee,5")
        val result = OfficeTool(root).execute(AgentTask("office", "读取 budget.csv"))
        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("coffee,5"))
    }

    @Test
    fun githubRejectsMissingRepository() = runBlocking {
        val result = GitHubTool().execute(AgentTask("github", "查看 GitHub"))
        assertTrue(result is ToolResult.Failure)
    }

    @Test
    fun githubFetchesInjectedRepositoryResponse() = runBlocking {
        val result = GitHubTool { FakeConnection("https://api.github.com/repos/koayxinchun4-cmd/agent") }
            .execute(AgentTask("github", "查看 koayxinchun4-cmd/agent"))
        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("fake github response"))
    }

    private class FakeConnection(url: String) : HttpURLConnection(java.net.URL(url)) {
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy() = false
        override fun getResponseCode(): Int = 200
        override fun getInputStream() = "fake ${if (url.host == "example.com") "web" else "github"} response"
            .byteInputStream()
    }
}
