package com.example.agent.nexus.agent

import com.example.agent.nexus.io.AppPrivateWorkspace
import com.example.agent.nexus.tool.AgentTool
import com.example.agent.nexus.tool.ApprovalDecision
import com.example.agent.nexus.tool.DefaultToolRuntime
import com.example.agent.nexus.tool.InMemoryToolApprovalStore
import com.example.agent.nexus.tool.RiskLevel
import com.example.agent.nexus.tool.ToolResult
import com.example.agent.nexus.tool.ToolRuntimeResult
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentArchitectureV2Test {

    @Test
    fun riskyToolRequiresConfirmation() = runBlocking {
        val tool = TestTool(riskLevel = RiskLevel.REQUIRES_CONFIRMATION)
        val runtime = DefaultToolRuntime()

        val denied = runtime.execute("session-1", AgentTask("task-1", "run"), tool)
        assertTrue(denied is ToolRuntimeResult.Denied)

        val approved = runtime.execute(
            "session-1",
            AgentTask("task-1", "run"),
            tool,
            confirmationGranted = true
        )
        assertTrue(approved is ToolRuntimeResult.Success)
        assertEquals(1, tool.calls)
    }

    @Test
    fun sessionApprovalIsScopedToOneSession() = runBlocking {
        val store = InMemoryToolApprovalStore()
        store.put("s1", "tool", ApprovalDecision.APPROVED, com.example.agent.nexus.tool.ApprovalScope.SESSION)

        assertEquals(ApprovalDecision.APPROVED, store.get("s1", "tool"))
        assertEquals(null, store.get("s2", "tool"))
    }

    @Test
    fun workspaceRejectsTraversalAndAbsolutePaths() {
        val root = Files.createTempDirectory("nexus-workspace").toFile()
        val workspace = AppPrivateWorkspace(root)
        workspace.write("notes.txt", "hello")
        assertEquals("hello", workspace.read("notes.txt"))

        val traversal = runCatching { workspace.read("../outside.txt") }
        assertTrue(traversal.isFailure)

        val absolute = runCatching { workspace.read("/etc/hosts") }
        assertTrue(absolute.isFailure)
    }

    private class TestTool(
        override val riskLevel: RiskLevel = RiskLevel.SAFE,
        override val requiredPermissions: Set<String> = emptySet()
    ) : AgentTool {
        override val id: String = "test"
        override val name: String = "Test"
        override val description: String = "Test tool"
        var calls: Int = 0

        override suspend fun execute(task: AgentTask): ToolResult {
            calls += 1
            return ToolResult.Success("ok")
        }
    }
}
