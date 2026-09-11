package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppAgentToolTest {
    @Test
    fun extractsPackageFromExplicitMetadata() {
        val task = AgentTask(
            "app-1",
            "open app",
            metadata = mapOf(AppAgentTool.PACKAGE_KEY to "com.example.demo")
        )

        assertEquals("com.example.demo", AppAgentTool.extractPackageName(task))
    }

    @Test
    fun extractsPackageFromExplicitTaskSyntax() {
        val task = AgentTask("app-2", "請開啟 package:com.example.demo")

        assertEquals("com.example.demo", AppAgentTool.extractPackageName(task))
    }

    @Test
    fun rejectsInvalidPackageMetadata() {
        val task = AgentTask(
            "app-3",
            "open app",
            metadata = mapOf(AppAgentTool.PACKAGE_KEY to "not a package")
        )

        assertNull(AppAgentTool.extractPackageName(task))
    }

    @Test
    fun rejectsTaskWithoutExplicitPackage() {
        val task = AgentTask("app-4", "open my browser")

        assertNull(AppAgentTool.extractPackageName(task))
    }
}
