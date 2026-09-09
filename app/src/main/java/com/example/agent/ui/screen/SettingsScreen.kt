package com.example.agent.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Language",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "Choose the language used by the Nexus interface. English is the primary product language.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NexusLanguage.entries.forEach { option ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onLanguageSelected(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (language == option) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = language == option,
                        onClick = { onLanguageSelected(option) }
                    )
                    Column(Modifier.padding(top = 12.dp, bottom = 12.dp, end = 12.dp)) {
                        Text(option.englishName, style = MaterialTheme.typography.titleMedium)
                        Text(option.nativeName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Code: ${option.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Technical Definition", style = MaterialTheme.typography.titleMedium)
                Text(
                    NexusDefinitions.LANGUAGE_SETTING,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Definitions are intentionally kept in English so technical meaning stays stable across UI languages.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
