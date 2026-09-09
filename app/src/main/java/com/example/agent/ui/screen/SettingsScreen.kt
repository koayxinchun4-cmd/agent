package com.example.agent.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NexusSettingsScreen(
    language: NexusLanguage,
    onLanguageSelected: (NexusLanguage) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Text(
            "Choose the language for the Nexus interface.",
            modifier = Modifier.padding(top = 8.dp)
        )

        NexusLanguage.entries.forEach { option ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                RadioButton(
                    selected = language == option,
                    onClick = { onLanguageSelected(option) }
                )
                Column(Modifier.padding(start = 8.dp)) {
                    Text(option.nativeName)
                    Text(option.englishName)
                }
            }
        }

        Text(
            NexusDefinitions.LANGUAGE_SETTING,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
