package com.example.agent.nexus.tool

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ToolCallTest {
    @Test
    fun creates_call_with_stable_identity() {
        val call = ToolCall(taskId = "task-1", toolId = "github", stepIndex = 2)

        assertEquals("task-1", call.taskId)
        assertEquals("github", call.toolId)
        assertEquals(2, call.stepIndex)
    }

    @Test
    fun rejects_blank_task_or_tool_ids() {
        assertThrows(IllegalArgumentException::class.java) {
            ToolCall(taskId = "", toolId = "github")
        }
        assertThrows(IllegalArgumentException::class.java) {
            ToolCall(taskId = "task-1", toolId = "")
        }
    }

    @Test
    fun rejects_negative_step_index() {
        assertThrows(IllegalArgumentException::class.java) {
            ToolCall(taskId = "task-1", toolId = "github", stepIndex = -1)
        }
    }
}
