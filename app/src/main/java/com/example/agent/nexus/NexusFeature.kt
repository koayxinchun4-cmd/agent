package com.example.agent.nexus

/**
 * Product-level capabilities of Nexus. A feature can be introduced in the UI
 * before its Android integration is enabled, so the product can grow without
 * replacing the core chat experience.
 */
enum class NexusFeature(
    val title: String,
    val description: String,
    val symbol: String
) {
    CHAT("AI 对话", "与 Nexus 对话、保存历史并继续上下文", "AI"),
    WEB_RESEARCH("Web Research", "搜索网页、整理资料并生成摘要", "WEB"),
    FILE_AGENT("手机文件", "整理、分类和管理你授权的手机文件", "FILE"),
    APP_AGENT("App Agent", "通过 Android 正规能力协助操作 App", "APP"),
    SKILLS("Skills 技能", "安装、管理和组合可复用的 Agent 技能", "SKILL"),
    MEMORY("长期记忆", "保存用户主动授权的偏好与重要上下文", "MEM"),
    AI_MOMENTS("AI Moments", "记录 Agent 的任务进度、结果和精彩时刻", "MOMENT"),
    OFFICE("Office Agent", "处理学习与办公资料、文档和任务", "DOC"),
    GITHUB("GitHub / Codex", "查看仓库、Issues、PR 并辅助编程", "CODE"),
    VOICE("语音助手", "语音输入与语音播报", "VOICE"),
    COMMUNITY("AI 朋友圈", "浏览和分享安全的 AI 创作与 Agent Moments", "SOCIAL")
}
