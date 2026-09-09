package com.example.agent.nexus.skill

data class SkillDocument(
    val id: String,
    val title: String,
    val content: String,
    val sections: Map<String, String>
)
