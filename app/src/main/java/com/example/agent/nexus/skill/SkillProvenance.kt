package com.example.agent.nexus.skill

data class SkillProvenance(
    val skillId: String,
    val skillName: String,
    val sourceRepository: String,
    val sourcePath: String,
    val sourceCommitOrVersion: String,
    val license: String,
    val originalAuthor: String,
    val importDate: String,
    val modificationStatus: String,
    val nexusChanges: String,
    val removalStatus: String
)
