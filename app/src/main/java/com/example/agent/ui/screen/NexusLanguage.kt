package com.example.agent.ui.screen

enum class NexusLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    CHINESE("zh", "中文", "Chinese"),
    ENGLISH("en", "English", "English"),
    MALAY("ms", "Bahasa Melayu", "Malay")
}

/**
 * Technical Definition: English-only definitions are kept stable across UI languages.
 */
object NexusDefinitions {
    const val LANGUAGE_SETTING = "Language: Controls the language used by the Nexus interface."
}
