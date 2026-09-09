package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalTaskToolTest {
    @Test
    fun analyzesAndroidTaskLocally() = runBlocking {
        val result = LocalTaskTool().execute(
            AgentTask("test", "帮我分析 Android CI")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("分类：Android"))
        assertTrue(result.text.contains("检查 Android / Gradle / Compose 上下文"))
        assertTrue(result.text.contains("验证修改结果"))
    }

    @Test
    fun analyzesResearchTaskWithActionablePath() = runBlocking {
        val result = LocalTaskTool().execute(
            AgentTask("test", "搜索 Android CI 最佳实践")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("分类：Research"))
        assertTrue(result.text.contains("收集可信来源"))
        assertTrue(result.text.contains("交叉检查结果"))
    }

    @Test
    fun rejectsBlankTask() = runBlocking {
        val result = LocalTaskTool().execute(AgentTask("test", "   "))

        assertTrue(result is ToolResult.Failure)
    }
}
