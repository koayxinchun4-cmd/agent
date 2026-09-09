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
    CHAT("AI Chat", "Chat with Nexus, keep history, and continue context", "AI"),
    WEB_RESEARCH("Web Research", "Search the web, organize sources, and create summaries", "WEB"),
    FILE_AGENT("File Agent", "Organize, classify, and manage user-authorized phone files", "FILE"),
    APP_AGENT("App Agent", "Assist with apps through supported Android capabilities", "APP"),
    SKILLS("Skills", "Install, manage, and compose reusable Agent skills", "SKILL"),
    MEMORY("Memory", "Store user-authorized preferences and important context", "MEM"),
    AI_MOMENTS("AI Moments", "Record Agent task progress, results, and memorable moments", "MOMENT"),
    OFFICE("Office Agent", "Work with study and office documents and tasks", "DOC"),
    GITHUB("GitHub / Codex", "View repositories, Issues, PRs, and assist with coding", "CODE"),
    VOICE("Voice Assistant", "Provide voice input and spoken responses", "VOICE"),
    COMMUNITY("AI Community", "Browse and share safe AI creations and Agent Moments", "SOCIAL")
}
