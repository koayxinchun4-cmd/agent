package com.example.agent.nexus.skill

object SkillParser {
    fun parse(id: String, content: String): SkillDocument {
        val lines = content.lines()
        val title = lines.firstOrNull { it.startsWith("# ") }
            ?.removePrefix("# ")
            ?.trim()
            ?: id

        val sections = linkedMapOf<String, String>()
        var current: String? = null
        val buffer = StringBuilder()

        fun flush() {
            val key = current ?: return
            sections[key] = buffer.toString().trim()
            buffer.clear()
        }

        for (line in lines) {
            if (line.startsWith("## ")) {
                flush()
                current = line.removePrefix("## ").trim()
            } else if (current != null) {
                buffer.append(line).append('\n')
            }
        }
        flush()

        return SkillDocument(id = id, title = title, content = content, sections = sections)
    }
}
