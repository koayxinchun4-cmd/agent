package com.example.agent.nexus.skill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillParserTest {
    @Test
    fun parsesTitleAndSections() {
        val skill = SkillParser.parse(
            "demo",
            """# Demo Skill

## Trigger
Use for demos.

## Verification
Run tests.
""".trimIndent()
        )

        assertEquals("Demo Skill", skill.title)
        assertEquals("Use for demos.", skill.sections["Trigger"])
        assertEquals("Run tests.", skill.sections["Verification"])
        assertTrue(skill.sections.containsKey("Trigger"))
    }
}
