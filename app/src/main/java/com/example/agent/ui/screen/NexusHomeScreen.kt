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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    "Nexus",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    "你的手机 AI Agent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "理解目标、规划任务、调用工具，并在需要时验证与重试。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Agent Core", style = MaterialTheme.typography.titleLarge)
                        AssistChip(
                            onClick = {},
                            label = { Text("Online") }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Plan → Tool → Verify → Retry",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "当前优先把手机端体验做好；后台服务保持可选，不阻塞核心 App。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("开始任务 / AI 对话")
                    }
                }
            }
        }

        item {
            Text("快速入口", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAction(
                    title = "AI 对话",
                    subtitle = "Chat",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenChat
                )
                QuickAction(
                    title = "Skills",
                    subtitle = "能力",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onOpenFeature(NexusFeature.SKILLS)
                    }
                )
            }
        }

        item {
            Text("能力中心", style = MaterialTheme.typography.titleLarge)
        }

        items(NexusFeature.entries) { feature ->
            FeatureCard(feature = feature, onClick = { onOpenFeature(feature) })
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FeatureCard(feature: NexusFeature, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${feature.symbol}  ${feature.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    feature.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedButton(onClick = onClick) {
                Text("打开")
            }
        }
    }
}
