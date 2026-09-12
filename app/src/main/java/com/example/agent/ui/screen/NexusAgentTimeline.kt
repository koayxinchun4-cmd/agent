package com.example.agent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.nexus.agent.AgentExecution
import com.example.agent.nexus.agent.AgentProgress

private data class TimelineItem(
    val title: String,
    val detail: String,
    val status: NexusTimelineStatus
)

@Composable
fun NexusAgentTimeline(
    execution: AgentExecution?,
    progress: AgentProgress?
) {
    val steps = execution?.steps.orEmpty().ifEmpty { progress?.steps.orEmpty() }
    val liveStep = progress?.step?.step
    val has = { name: String -> steps.any { it.step == name || it.step.startsWith(name) } }
    val failed = { name: String -> steps.lastOrNull { it.step == name || it.step.startsWith(name) }?.success == false }
    val active = { name: String -> liveStep == name || liveStep?.startsWith(name) == true }

    val items = listOf(
        TimelineItem("理解需求", "分析目标与约束", nexusTimelineStatus(has("understand_request"), active("understand_request"), false)),
        TimelineItem("制定计划", "拆分任务与选择执行路径", nexusTimelineStatus(has("plan"), active("plan"), false)),
        TimelineItem("执行工具", "调用可用 Tools / Models", nexusTimelineStatus(has("use_tool:"), active("use_tool:"), failed("use_tool:"))),
        TimelineItem("验证结果", "检查执行结果是否满足目标", nexusTimelineStatus(has("verify"), active("verify"), failed("verify"))),
        TimelineItem("完成", "整理最终结果并交给你", nexusTimelineStatus(has("answer"), active("answer"), failed("answer")))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("正在执行任务", style = MaterialTheme.typography.titleLarge)
        items.forEachIndexed { index, item ->
            NexusTimelineItem(item, isLast = index == items.lastIndex)
        }
    }
}

@Composable
fun NexusConfirmationCard(
    toolName: String,
    action: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("需要你的确认", style = MaterialTheme.typography.titleLarge)
            Text(toolName, style = MaterialTheme.typography.titleMedium)
            Text(action, style = MaterialTheme.typography.bodyLarge)
            Text(
                "这个操作需要明确确认后才能执行。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("取消") }
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("确认执行") }
            }
        }
    }
}

@Composable
private fun NexusTimelineItem(item: TimelineItem, isLast: Boolean) {
    val (symbol, containerColor) = when (item.status) {
        NexusTimelineStatus.DONE -> "✓" to MaterialTheme.colorScheme.secondaryContainer
        NexusTimelineStatus.ACTIVE -> "●" to MaterialTheme.colorScheme.primaryContainer
        NexusTimelineStatus.PENDING -> "○" to MaterialTheme.colorScheme.surfaceVariant
        NexusTimelineStatus.FAILED -> "!" to MaterialTheme.colorScheme.errorContainer
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(38.dp).background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(symbol, style = MaterialTheme.typography.titleMedium) }
            if (!isLast) {
                Box(
                    modifier = Modifier.padding(vertical = 3.dp).size(width = 2.dp, height = 28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (item.status) {
                    NexusTimelineStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                    NexusTimelineStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(3.dp))
                Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
