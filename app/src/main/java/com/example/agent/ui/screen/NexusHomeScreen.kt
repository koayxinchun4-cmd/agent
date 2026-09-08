package com.example.agent.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.nexus.NexusFeature

@Composable
fun NexusHomeScreen(
    onOpenChat: () -> Unit,
    onOpenFeature: (NexusFeature) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Nexus 智能助手", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "你的纯手机 AI Agent · 不需要电脑 · 不需要 Root",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Team Leader", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("告诉 Nexus 你的目标，由 Agent Core 负责理解、规划、调用技能并汇报结果。")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onOpenChat, modifier = Modifier.fillMaxWidth()) {
                        Text("开始任务 / AI 对话")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("能力中心", style = MaterialTheme.typography.titleLarge)
        }

        items(NexusFeature.entries) { feature ->
            FeatureCard(feature = feature, onClick = { onOpenFeature(feature) })
        }
    }
}

@Composable
private fun FeatureCard(feature: NexusFeature, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp)) {
            Column(Modifier.weight(1f)) {
                Text("[${feature.symbol}]  ${feature.title}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(feature.description, style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = onClick) {
                Text("打开")
            }
        }
    }
}
