package com.vijay.tadashi.presentation.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownMessage(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val blocks = remember(markdown) { parseBlocks(markdown) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    }
                    Text(
                        text = buildInline(block.text, textColor),
                        style = style,
                        color = textColor
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildInline(block.text, textColor),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }

                is MarkdownBlock.Bullets -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = buildInline(item, textColor),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.Numbered -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = buildInline(item, textColor),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    CodeBlock(
                        code = block.code,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is MarkdownBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = buildInline(block.text, textColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val shape = RoundedCornerShape(12.dp)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy"
                )
            }
        }
        Text(
            text = code,
            modifier = Modifier.horizontalScroll(scrollState),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            softWrap = false,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip
        )
    }
}

private sealed interface MarkdownBlock {
    data class Heading(
        val level: Int,
        val text: String
    ) : MarkdownBlock

    data class Paragraph(
        val text: String
    ) : MarkdownBlock

    data class Bullets(
        val items: List<String>
    ) : MarkdownBlock

    data class Numbered(
        val items: List<String>
    ) : MarkdownBlock

    data class CodeBlock(
        val code: String
    ) : MarkdownBlock

    data class BlockQuote(
        val text: String
    ) : MarkdownBlock
}

private fun parseBlocks(input: String): List<MarkdownBlock> {
    val lines = input.replace("\r\n", "\n").split("\n")
    val blocks = mutableListOf<MarkdownBlock>()

    var i = 0
    var inCode = false
    val codeBuffer = StringBuilder()

    fun flushParagraph(buffer: StringBuilder) {
        if (buffer.isNotBlank()) {
            blocks += MarkdownBlock.Paragraph(buffer.toString().trim())
            buffer.clear()
        }
    }

    val paragraphBuffer = StringBuilder()

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trimEnd()

        if (trimmed.trim().startsWith("```")) {
            if (!inCode) {
                flushParagraph(paragraphBuffer)
                inCode = true
                codeBuffer.clear()
            } else {
                inCode = false
                blocks += MarkdownBlock.CodeBlock(codeBuffer.toString().trimEnd())
                codeBuffer.clear()
            }
            i++
            continue
        }

        if (inCode) {
            codeBuffer.append(trimmed).append('\n')
            i++
            continue
        }

        if (trimmed.isBlank()) {
            flushParagraph(paragraphBuffer)
            i++
            continue
        }

        val headingMatch = Regex("^(#{1,6})\\s+(.*)$").find(trimmed.trim())
        if (headingMatch != null) {
            flushParagraph(paragraphBuffer)
            val level = headingMatch.groupValues[1].length
            val text = headingMatch.groupValues[2]
            blocks += MarkdownBlock.Heading(level = level, text = text)
            i++
            continue
        }

        val bulletMatch = Regex("^([-*])\\s+(.*)$").find(trimmed.trim())
        if (bulletMatch != null) {
            flushParagraph(paragraphBuffer)
            val items = mutableListOf<String>()
            var j = i
            while (j < lines.size) {
                val l = lines[j].trim()
                val m = Regex("^([-*])\\s+(.*)$").find(l) ?: break
                items += m.groupValues[2]
                j++
            }
            blocks += MarkdownBlock.Bullets(items)
            i = j
            continue
        }

        val numberedMatch = Regex("^(\\d+)\\.\\s+(.*)$").find(trimmed.trim())
        if (numberedMatch != null) {
            flushParagraph(paragraphBuffer)
            val items = mutableListOf<String>()
            var j = i
            while (j < lines.size) {
                val l = lines[j].trim()
                val m = Regex("^(\\d+)\\.\\s+(.*)$").find(l) ?: break
                items += m.groupValues[2]
                j++
            }
            blocks += MarkdownBlock.Numbered(items)
            i = j
            continue
        }

        val quoteMatch = Regex("^>\\s?(.*)$").find(trimmed.trim())
        if (quoteMatch != null) {
            flushParagraph(paragraphBuffer)
            val items = mutableListOf<String>()
            var j = i
            while (j < lines.size) {
                val l = lines[j].trim()
                val m = Regex("^>\\s?(.*)$").find(l) ?: break
                items += m.groupValues[1]
                j++
            }
            blocks += MarkdownBlock.BlockQuote(items.joinToString("\n").trim())
            i = j
            continue
        }

        if (paragraphBuffer.isNotEmpty()) paragraphBuffer.append('\n')
        paragraphBuffer.append(trimmed)
        i++
    }

    flushParagraph(paragraphBuffer)

    if (inCode && codeBuffer.isNotBlank()) {
        blocks += MarkdownBlock.CodeBlock(codeBuffer.toString().trimEnd())
    }

    return blocks
}

private fun buildInline(text: String, textColor: androidx.compose.ui.graphics.Color): AnnotatedString {
    val parts = text.split("`")
    val codeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = androidx.compose.ui.graphics.Color(0x33000000),
        color = textColor,
        fontWeight = FontWeight.Medium
    )
    val boldStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = textColor
    )
    val italicStyle = SpanStyle(
        fontStyle = FontStyle.Italic,
        color = textColor
    )

    return buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                var i = 0
                while (i < part.length) {
                    if (part.startsWith("**", i)) {
                        val end = part.indexOf("**", startIndex = i + 2)
                        if (end != -1) {
                            pushStyle(boldStyle)
                            append(part.substring(i + 2, end))
                            pop()
                            i = end + 2
                            continue
                        }
                    }

                    if (part.startsWith("*", i)) {
                        val end = part.indexOf("*", startIndex = i + 1)
                        if (end != -1) {
                            pushStyle(italicStyle)
                            append(part.substring(i + 1, end))
                            pop()
                            i = end + 1
                            continue
                        }
                    }

                    append(part[i])
                    i++
                }
            } else {
                pushStyle(codeStyle)
                append(part)
                pop()
            }
        }
    }
}
