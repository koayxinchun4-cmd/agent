package com.example.agent.nexus.agent

import com.example.agent.nexus.tool.AgentTool
import com.example.agent.nexus.tool.ToolResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentLoopTest {

    @Test
    fun successfulToolDoesNotRetry() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Success("找到结果")
        )
        val agent = agentWith(tool)

        val execution = agent.executeDetailed(AgentTask("1", "搜索 Nexus"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(1, execution.attempts)
        assertEquals(1, tool.calls)
        assertTrue(execution.steps.any { it.step == "verify" && it.success })
    }

    @Test
    fun failedFirstAttemptRetriesAndThenSucceeds() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Failure("暂时失败"),
            ToolResult.Success("第二次成功")
        )
        val agent = agentWith(tool)

        val execution = agent.executeDetailed(AgentTask("2", "搜索 Nexus"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(2, execution.attempts)
        assertEquals(2, tool.calls)
        assertTrue(execution.steps.any { it.output.contains("retrying") })
    }

    @Test
    fun twoFailuresEndWithFailure() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Failure("第一次失败"),
            ToolResult.Failure("第二次失败")
        )
        val agent = agentWith(tool)

        val execution = agent.executeDetailed(AgentTask("3", "搜索 Nexus"))

        assertTrue(execution.result is AgentResult.Failure)
        assertEquals(2, execution.attempts)
        assertEquals(2, tool.calls)
        assertTrue(execution.steps.any { it.output.contains("maximum attempts reached") })
    }

    @Test
    fun blankSuccessIsRejectedAndRetried() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Success("   "),
            ToolResult.Success("有效结果")
        )
        val agent = agentWith(tool)

        val execution = agent.executeDetailed(AgentTask("4", "搜索 Nexus"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(2, execution.attempts)
        assertEquals(2, tool.calls)
        assertTrue(execution.steps.any { it.output.contains("工具返回了空結果") })
    }

    @Test
    fun maxAttemptsOnePreventsRetry() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Failure("失败"),
            ToolResult.Success("不应该执行")
        )
        val agent = agentWith(tool, AgentLoopConfig(maxAttempts = 1))

        val execution = agent.executeDetailed(AgentTask("5", "搜索 Nexus"))

        assertTrue(execution.result is AgentResult.Failure)
        assertEquals(1, execution.attempts)
        assertEquals(1, tool.calls)
    }

    private fun agentWith(tool: AgentTool, config: AgentLoopConfig = AgentLoopConfig()): NexusAgent {
        return NexusAgent(
            toolRegistry = com.example.agent.nexus.tool.ToolRegistry(listOf(tool)),
            loopConfig = config
        )
    }

    private class SequenceTool(
        override val id: String,
        private vararg val results: ToolResult
    ) : AgentTool {
        override val name: String = "Test Tool"
        override val description: String = "Agent Loop test tool"

        var calls: Int = 0
            private set

        override suspend fun execute(task: AgentTask): ToolResult {
            val index = calls.coerceAtMost(results.lastIndex)
            calls += 1
            return results[index]
        }
    }
}
