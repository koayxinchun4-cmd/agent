package com.example.agent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class TaskStepStatus {
    DONE,
    ACTIVE,
    PENDING
}

private data class TaskStep(
    val title: String,
    val detail: String,
    val status: TaskStepStatus
)

@Composable
fun TaskScreen(
    initialPrompt: String = "",
    onBack: () -> Unit
) {
    var prompt by remember { mutableStateOf(initialPrompt) }
    var started by remember { mutableStateOf(initialPrompt.isNotBlank()) }

    val steps = listOf(
        TaskStep("理解需求", "分析目标与约束", if (started) TaskStepStatus.DONE else TaskStepStatus.ACTIVE),
        TaskStep("制定计划", "拆分任务与选择执行路径", if (started) TaskStepStatus.ACTIVE else TaskStepStatus.PENDING),
        TaskStep("执行工具", "调用可用 Tools / Models", TaskStepStatus.PENDING),
        TaskStep("验证结果", "检查执行结果是否满足目标", TaskStepStatus.PENDING),
        TaskStep("完成", "整理最终结果并交给你", TaskStepStatus.PENDING)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Nexus Task", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (started) "Agent workflow" else "Give Nexus a goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBack) {
                Text("返回")
            }
        }

        if (!started) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("What should Nexus do?", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("例如：帮我分析这个 Android CI 失败的原因") }
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { started = true },
                        enabled = prompt.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Start task")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Task", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(prompt, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "运行中 · Planner 正在选择下一步",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Text("Agent workflow", style = MaterialTheme.typography.titleLarge)

            steps.forEachIndexed { index, step ->
                TaskStepCard(step, isLast = index == steps.lastIndex)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Current action", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("Planning next action…", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "后续会在这里接入 NexusAgent、AgentPlanner、ModelRouter 和 ToolRegistry 的真实执行状态。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = { started = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("停止 / 修改任务")
            }
        }
    }
}

@Composable
private fun TaskStepCard(step: TaskStep, isLast: Boolean) {
    val (symbol, containerColor) = when (step.status) {
        TaskStepStatus.DONE -> "✓" to MaterialTheme.colorScheme.secondaryContainer
        TaskStepStatus.ACTIVE -> "●" to MaterialTheme.colorScheme.primaryContainer
        TaskStepStatus.PENDING -> "○" to MaterialTheme.colorScheme.surfaceVariant
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, style = MaterialTheme.typography.titleMedium)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .size(width = 2.dp, height = 28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    step.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
