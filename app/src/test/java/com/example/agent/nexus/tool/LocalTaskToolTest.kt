package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalTaskToolTest {
    @Test
    fun analyzesAndroidTaskLocally() = runBlocking {
        val result = LocalTaskTool().execute(
            AgentTask("test", "分析 Android CI")
        )

        assertTrue(result is ToolResult.Success)
        assertTrue((result as ToolResult.Success).text.contains("分類：Android"))
        assertTrue(result.text.contains("Nexus Local Tool"))
        assertTrue(result.text.contains("下一步：等待對應專業 Tool 接入後繼續執行。"))
    }

    @Test
    fun rejectsBlankTask() = runBlocking {
        val result = LocalTaskTool().execute(AgentTask("test", "   "))

        assertTrue(result is ToolResult.Failure)
    }
}
