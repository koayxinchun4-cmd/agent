package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolCallerTest {
    @Test
    fun executes_registered_tool_for_matching_task() = runBlocking {
        val tool = RecordingTool("github")
        val caller = ToolCaller(ToolRegistry(listOf(tool)))
        val task = AgentTask(id = "task-1", input = "check CI")

        val result = caller.execute(ToolCall(task.id, tool.id), task)

        assertEquals(ToolResult.Success("done"), result)
        assertEquals(task, tool.lastTask)
    }

    @Test
    fun rejects_call_for_different_task() = runBlocking {
        val caller = ToolCaller(ToolRegistry())
        val task = AgentTask(id = "task-1", input = "check CI")

        val error = runCatching {
            caller.execute(ToolCall("task-2", "github"), task)
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun propagates_registry_failure() = runBlocking {
        val caller = ToolCaller(ToolRegistry())
        val task = AgentTask(id = "task-1", input = "check CI")

        val result = caller.execute(ToolCall(task.id, "missing"), task)

        assertEquals(ToolResult.Failure("Nexus 找不到工具：missing"), result)
    }

    private class RecordingTool(override val id: String) : AgentTool {
        override val name = "Recording tool"
        override val description = "Test tool"
        var lastTask: AgentTask? = null

        override suspend fun execute(task: AgentTask): ToolResult {
            lastTask = task
            return ToolResult.Success("done")
        }
    }
}
