package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.AgentTool
import com.example.agent.nexus.tool.RiskLevel
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.nexus.tool.ToolResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusAgentReliabilityTest {
    @Test
    fun retriesTransientFailureAfterDiagnosisAndInputAdjustment() = runBlocking {
        var calls = 0
        var secondInput = ""
        val tool = object : AgentTool {
            override val id = "local_task"
            override val name = "Test Task"
            override val description = "Deterministic test tool"

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                if (calls == 2) secondInput = task.input
                return if (calls == 1) {
                    ToolResult.Failure("temporary network failure")
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
        assertTrue(execution.steps.any { it.step == "diagnose" && it.output.contains("Transient") })
        assertTrue(execution.steps.any { it.step == "adjust_plan" && it.success })
        assertTrue(secondInput.contains("Recovery attempt 1"))
    }

    @Test
    fun permanentFailureStopsWithoutUnboundedRetry() = runBlocking {
        var calls = 0
        val tool = object : AgentTool {
            override val id = "local_task"
            override val name = "Test Task"
            override val description = "Permanent failure"

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                return ToolResult.Failure("invalid input")
            }
        }

        val execution = NexusAgent(
            toolRegistry = ToolRegistry(listOf(tool)),
            loopConfig = AgentLoopConfig(maxAttempts = 5)
        ).executeDetailed(AgentTask("reliability-2", "run a task"))

        assertTrue(execution.result is AgentResult.Failure)
        assertEquals(1, execution.attempts)
        assertEquals(1, calls)
        assertTrue(execution.steps.any { it.step == "diagnose" && it.output.contains("Permanent") })
    }

    @Test
    fun verifierFailureEntersRecoveryAndRetries() = runBlocking {
        var calls = 0
        var secondInput = ""
        val tool = object : AgentTool {
            override val id = "local_task"
            override val name = "Test Task"
            override val description = "Verifier recovery test"

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                if (calls == 2) secondInput = task.input
                return if (calls == 1) ToolResult.Success("   ") else ToolResult.Success("valid")
            }
        }

        val execution = NexusAgent(
            toolRegistry = ToolRegistry(listOf(tool)),
            loopConfig = AgentLoopConfig(maxAttempts = 2)
        ).executeDetailed(AgentTask("reliability-3", "run a task"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(2, execution.attempts)
        assertEquals(2, calls)
        assertTrue(secondInput.contains("Recovery attempt 1"))
        assertTrue(execution.steps.any { it.step == "diagnose" })
    }

    @Test
    fun riskyToolCannotExecuteWithoutConfirmation() = runBlocking {
        var calls = 0
        val tool = object : AgentTool {
            override val id = "risky"
            override val name = "Risky Tool"
            override val description = "Requires confirmation"
            override val riskLevel = RiskLevel.REQUIRES_CONFIRMATION

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                return ToolResult.Success("changed")
            }
        }

        val result = ToolRegistry(listOf(tool)).execute("risky", AgentTask("safe", "do it"))

        assertTrue(result is ToolResult.Failure)
        assertEquals(0, calls)
    }

    @Test
    fun confirmedRiskyToolCanExecute() = runBlocking {
        var calls = 0
        val tool = object : AgentTool {
            override val id = "risky"
            override val name = "Risky Tool"
            override val description = "Requires confirmation"
            override val riskLevel = RiskLevel.REQUIRES_CONFIRMATION

            override suspend fun execute(task: AgentTask): ToolResult {
                calls += 1
                return ToolResult.Success("changed")
            }
        }

        val task = AgentTask(
            "safe",
            "do it",
            mapOf(AgentTask.CONFIRMATION_GRANTED to "true")
        )
        val result = ToolRegistry(listOf(tool)).execute("risky", task)

        assertTrue(result is ToolResult.Success)
        assertEquals(1, calls)
    }

    @Test
    fun rejectsInvalidLoopConfiguration() {
        assertThrows(IllegalArgumentException::class.java) {
            AgentLoopConfig(maxAttempts = 0)
        }
    }
}
