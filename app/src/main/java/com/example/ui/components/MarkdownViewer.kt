package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.IntentHelper

@Composable
fun MarkdownViewer(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onCommitCode: ((code: String, language: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val sections = parseMarkdownBlocks(content)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        sections.forEach { section ->
            when (section) {
                is Block.Code -> {
                    CodeBlockView(
                        language = section.language,
                        code = section.code,
                        onCopy = {
                            IntentHelper.copyToClipboard(context, "代码片段", section.code)
                        },
                        onCommitToGitHub = if (onCommitCode != null) {
                            { onCommitCode(section.code, section.language) }
                        } else null
                    )
                }
                is Block.Heading -> {
                    val fontSize = when (section.level) {
                        1 -> 20.sp
                        2 -> 17.sp
                        else -> 15.sp
                    }
                    Text(
                        text = section.text,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is Block.ListItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (section.isOrdered) "${section.index}. " else "• ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        FormattedInlineText(
                            text = section.text,
                            textColor = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is Block.Paragraph -> {
                    FormattedInlineText(
                        text = section.text,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit,
    onCommitToGitHub: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "代码块",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language.isNotBlank()) language else "code",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onCommitToGitHub != null) {
                        IconButton(
                            onClick = onCommitToGitHub,
                            modifier = Modifier.size(24.dp).testTag("commit_code_github_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "提交到 GitHub 仓库",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp).testTag("copy_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制代码",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun FormattedInlineText(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        var i = 0
        val length = text.length
        while (i < length) {
            when {
                // Bold `**text**`
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline Code `` `code` ``
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x3300E5FF),
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic `*text*`
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = textColor,
        modifier = modifier
    )
}

sealed class Block {
    data class Code(val language: String, val code: String) : Block()
    data class Heading(val level: Int, val text: String) : Block()
    data class ListItem(val text: String, val isOrdered: Boolean, val index: Int = 0) : Block()
    data class Paragraph(val text: String) : Block()
}

fun parseMarkdownBlocks(raw: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val lines = raw.lines()
    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        if (line.trimStart().startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(Block.Code(codeLang, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLang = line.trim().removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            codeBuilder.append(line).append("\n")
            i++
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> blocks.add(Block.Heading(1, trimmed.removePrefix("# ").trim()))
            trimmed.startsWith("## ") -> blocks.add(Block.Heading(2, trimmed.removePrefix("## ").trim()))
            trimmed.startsWith("### ") -> blocks.add(Block.Heading(3, trimmed.removePrefix("### ").trim()))
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                blocks.add(Block.ListItem(trimmed.substring(2).trim(), isOrdered = false))
            }
            trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                val match = Regex("^(\\d+)\\.\\s(.*)").find(trimmed)
                if (match != null) {
                    val index = match.groupValues[1].toIntOrNull() ?: 1
                    val text = match.groupValues[2]
                    blocks.add(Block.ListItem(text, isOrdered = true, index = index))
                } else {
                    blocks.add(Block.Paragraph(line))
                }
            }
            trimmed.isBlank() -> {
                // skip empty lines
            }
            else -> {
                blocks.add(Block.Paragraph(line))
            }
        }
        i++
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        blocks.add(Block.Code(codeLang, codeBuilder.toString().trimEnd()))
    }

    return blocks
}
