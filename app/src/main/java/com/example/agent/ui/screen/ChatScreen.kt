package com.example.agent.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.data.local.ChatMessage

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatHeader()
        HorizontalDivider()

        if (messages.isEmpty()) {
            EmptyChatContent(
                onSuggestion = { inputText = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }
        }

        ChatComposer(
            inputText = inputText,
            onInputChange = { inputText = it },
            isSending = isSending,
            onSend = {
                viewModel.sendMessage(inputText.trim())
                inputText = ""
            }
        )

        TextButton(
            onClick = { viewModel.clearHistory() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("清除对话历史")
        }
    }
}

@Composable
private fun ChatHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Nexus Chat", style = MaterialTheme.typography.titleLarge)
            Text(
                "AI Agent 对话",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                "● 就绪",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyChatContent(
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hi，我是 Nexus", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "告诉我你的目标。我会优先考虑手机、免 Root 的执行方式。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("试试看", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { onSuggestion("帮我规划一个任务") },
                label = { Text("规划任务") }
            )
            AssistChip(
                onClick = { onSuggestion("帮我搜索网页资料") },
                label = { Text("网页研究") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { onSuggestion("帮我整理手机文件") },
                label = { Text("整理文件") }
            )
            AssistChip(
                onClick = { onSuggestion("介绍一下 Nexus 的 Skills") },
                label = { Text("Skills") }
            )
        }
    }
}

@Composable
private fun ChatComposer(
    inputText: String,
    onInputChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("告诉 Nexus 你的目标…") },
            enabled = !isSending,
            maxLines = 4,
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            enabled = inputText.isNotBlank() && !isSending,
            onClick = onSend,
            shape = RoundedCornerShape(18.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("发送")
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
