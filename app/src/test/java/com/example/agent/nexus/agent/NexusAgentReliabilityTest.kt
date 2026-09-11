package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.AgentTool
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusAgentReliabilityTest {
    @Test
    fun retriesFailedToolAndSucceedsOnSecondAttempt() = runBlocking {
        var calls = 0
        val tool = object : AgentTool {
            override val id = "local_task"
            override val name = "Test Task"
            override val description = "Deterministic test tool"

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                return if (calls == 1) {
                    ToolResult.Failure("temporary failure")
                } else {
                    ToolResult.Success("completed")
                }
            }
        }

        val execution = NexusAgent(
            toolRegistry = ToolRegistry(listOf(tool)),
            loopConfig = AgentLoopConfig(maxAttempts = 2)
        ).executeDetailed(AgentTask("reliability-1", "run a task"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(2, execution.attempts)
        assertEquals(2, calls)
        assertTrue(execution.steps.any { it.step == "verify" && it.message.contains("retrying") })
    }

    @Test
    fun stopsAfterConfiguredMaximumAttempts() = runBlocking {
        var calls = 0
        val tool = object : AgentTool {
            override val id = "local_task"
            override val name = "Test Task"
            override val description = "Always fails"

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                return ToolResult.Failure("persistent failure")
            }
        }

        val execution = NexusAgent(
            toolRegistry = ToolRegistry(listOf(tool)),
            loopConfig = AgentLoopConfig(maxAttempts = 2)
        ).executeDetailed(AgentTask("reliability-2", "run a task"))

        assertTrue(execution.result is AgentResult.Failure)
        assertEquals(2, execution.attempts)
        assertEquals(2, calls)
        assertTrue(execution.steps.last().step == "answer")
    }

    @Test
    fun rejectsInvalidLoopConfiguration() {
        try {
            AgentLoopConfig(maxAttempts = 0)
            throw AssertionError("Expected invalid configuration to be rejected")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("maxAttempts"))
        }
    }
}
