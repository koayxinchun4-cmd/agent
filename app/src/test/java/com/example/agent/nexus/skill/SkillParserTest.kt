package com.example.agent.nexus.skill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillParserTest {
    @Test
    fun parsesTitleAndSections() {
        val skill = SkillParser.parse(
            "demo",
            "# Demo Skill\n\n## Trigger\nUse for demo tasks.\n\n## Verification\nRun tests."
        )

        assertEquals("Demo Skill", skill.title)
        assertEquals("Use for demo tasks.", skill.sections["Trigger"])
        assertTrue(skill.sections["Verification"]!!.contains("Run tests."))
    }
}
