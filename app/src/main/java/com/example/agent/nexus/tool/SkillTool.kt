package com.example.agent.nexus.tool

import com.example.agent.nexus.agent.AgentTask
import com.example.agent.nexus.skill.SkillRegistry

class SkillTool(
    private val registry: SkillRegistry
) : AgentTool {
    override val id: String = "skills"
    override val name: String = "Nexus Skills"
    override val description: String = "Lists and reads local SKILL.md files from the Nexus skill registry."

    override suspend fun execute(task: AgentTask): ToolResult {
        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("技能任务内容不能为空")

        return runCatching {
            when {
                input.contains("列表") || input.contains("list", ignoreCase = true) -> listSkills()
                input.contains("读取") || input.contains("read", ignoreCase = true) -> readSkill(input)
                else -> listSkills()
            }
        }.getOrElse { ToolResult.Failure("技能工具执行失败：${it.message ?: "未知错误"}", it) }
    }

    private fun listSkills(): ToolResult {
        val skills = registry.list()
        if (skills.isEmpty()) return ToolResult.Success("Nexus Skill Registry 目前没有已安装技能。")
        return ToolResult.Success(
            "Nexus Skills\n" + skills.joinToString("\n") { "- ${it.id}: ${it.title}" }
        )
    }

    private fun readSkill(input: String): ToolResult {
        val id = input.substringAfter("读取", "").trim()
            .ifEmpty { input.substringAfter("read", "").trim() }
        if (id.isEmpty()) return ToolResult.Failure("请提供要读取的 Skill ID")
        val skill = registry.get(id) ?: return ToolResult.Failure("找不到 Skill：$id")
        val sections = skill.sections.entries.joinToString("\n\n") { (name, body) -> "## $name\n$body" }
        return ToolResult.Success("${skill.title}\n\n$sections")
    }
}
