package com.example.agent.nexus.skill

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillRegistryTest {
    @Test
    fun installedSkillDefaultsToEnabledForBackwardCompatibility() {
        val root = Files.createTempDirectory("skills").toFile()
        val registry = SkillRegistry(root)
        registry.install("demo", "# Demo\n\nUse for tests.")

        assertTrue(registry.isEnabled("demo"))
    }

    @Test
    fun disablingAndEnablingSkillPersistsState() {
        val root = Files.createTempDirectory("skills").toFile()
        val registry = SkillRegistry(root)
        registry.install("demo", "# Demo\n\nUse for tests.")

        registry.disable("demo")
        assertFalse(registry.isEnabled("demo"))

        val reloaded = SkillRegistry(root)
        assertFalse(reloaded.isEnabled("demo"))

        reloaded.enable("demo")
        assertTrue(SkillRegistry(root).isEnabled("demo"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun enablingMissingSkillFails() {
        val root = Files.createTempDirectory("skills").toFile()
        SkillRegistry(root).enable("missing")
    }
}
