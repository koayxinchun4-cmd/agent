package com.example.agent.nexus.tool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        assertFailsWith<IllegalArgumentException> {
            ToolCall(taskId = "", toolId = "github")
        }
        assertFailsWith<IllegalArgumentException> {
            ToolCall(taskId = "task-1", toolId = "")
        }
    }

    @Test
    fun rejects_negative_step_index() {
        assertFailsWith<IllegalArgumentException> {
            ToolCall(taskId = "task-1", toolId = "github", stepIndex = -1)
        }
    }
}
