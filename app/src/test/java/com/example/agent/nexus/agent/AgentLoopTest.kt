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
    fun twoTransientFailuresEndAtConfiguredBound() = runBlocking {
        val tool = SequenceTool(
            "web_research",
            ToolResult.Failure("第一次暂时失败"),
            ToolResult.Failure("第二次暂时失败")
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
        assertTrue(execution.steps.any { it.output.contains("工具返回了空结果") })
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

    @Test
    fun decomposedToolSubtasksExecuteInOrder() = runBlocking {
        val first = SequenceTool("web_research", ToolResult.Success("研究结果"))
        val second = SequenceTool("github", ToolResult.Success("CI 結果"))
        val agent = agentWith(first, second)

        val execution = agent.executeDetailed(
            AgentTask("6", "搜尋 repo then 檢查 CI then 整理結果")
        )

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(1, first.calls)
        assertEquals(1, second.calls)
        assertEquals("搜尋 repo", first.inputs.single())
        assertEquals("檢查 CI", second.inputs.single())
        assertTrue(execution.steps.any { it.output.contains("subtask 1") })
        assertTrue(execution.steps.any { it.output.contains("subtask 2") })
        assertTrue(execution.steps.any { it.step == "model:local" })
    }

    @Test
    fun singleSubtaskLocalFallbackStillExecutesLocalTask() = runBlocking {
        val tool = SequenceTool("local_task", ToolResult.Success("本機任務完成"))
        val agent = agentWith(tool)

        val execution = agent.executeDetailed(AgentTask("7", "完成一個一般任務"))

        assertTrue(execution.result is AgentResult.Success)
        assertEquals(1, tool.calls)
        assertEquals("完成一個一般任務", tool.inputs.single())
    }

    private fun agentWith(
        vararg tools: AgentTool,
        config: AgentLoopConfig = AgentLoopConfig()
    ): NexusAgent {
        return NexusAgent(
            toolRegistry = com.example.agent.nexus.tool.ToolRegistry(tools.toList()),
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
        val inputs = mutableListOf<String>()

        override suspend fun execute(task: AgentTask): ToolResult {
            val index = calls.coerceAtMost(results.lastIndex)
            calls += 1
            inputs += task.input
            return results[index]
        }
    }
}
