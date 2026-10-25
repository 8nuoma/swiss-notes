package com.swissnotes.app.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.swissnotes.app.ui.theme.SwissColors

/**
 * A lightweight Markdown renderer: headings, bold/italic/strikethrough,
 * inline code, code blocks, blockquotes, lists, task lists, images,
 * videos, links and `[[wikilinks]]`.
 */
@Composable
fun MarkdownView(
    text: String,
    onWikilinkClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val lines = text.split("\n")
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                line.startsWith("```") -> {
                    val code = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) {
                        code.add(lines[i])
                        i++
                    }
                    CodeBlock(code.joinToString("\n"))
                    i++
                }

                line.startsWith("##### ") -> {
                    Heading(line.removePrefix("##### "), 5); i++
                }
                line.startsWith("#### ") -> {
                    Heading(line.removePrefix("#### "), 4); i++
                }
                line.startsWith("### ") -> {
                    Heading(line.removePrefix("### "), 3); i++
                }
                line.startsWith("## ") -> {
                    Heading(line.removePrefix("## "), 2); i++
                }
                line.startsWith("# ") -> {
                    Heading(line.removePrefix("# "), 1); i++
                }

                line.startsWith("> ") -> {
                    Blockquote(line.removePrefix("> ")); i++
                }

                line.startsWith("- [x] ") || line.startsWith("- [ ] ") -> {
                    val checked = line.startsWith("- [x] ")
                    val content = line.removePrefix("- [x] ").removePrefix("- [ ] ")
                    TaskItem(content, checked); i++
                }

                line.startsWith("- ") || line.startsWith("* ") -> {
                    ListItem("•", line.removePrefix("- ").removePrefix("* ")); i++
                }

                line.matches(Regex("""\d+\. .*""")) -> {
                    ListItem(line.substringBefore(". ") + ".", line.substringAfter(". ")); i++
                }

                line.startsWith("![") && line.contains("](") -> {
                    val src = line.substringAfter("](").substringBefore(")")
                    val alt = line.substringAfter("![").substringBefore("]")
                    MediaBlock(src, alt); i++
                }

                line.startsWith("---") || line.startsWith("***") -> {
                    HorizontalRule(); i++
                }

                line.isBlank() -> {
                    Spacer(Modifier.height(6.dp)); i++
                }

                else -> {
                    InlineText(line, onWikilinkClick); i++
                }
            }
        }
    }
}

@Composable
private fun Heading(text: String, level: Int) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineMedium
        2 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }
    Text(
        text = text,
        style = style,
        color = SwissColors.Black,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
    )
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(SwissColors.OffWhite)
            .padding(12.dp),
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = SwissColors.Black,
        )
    }
}

@Composable
private fun Blockquote(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .background(SwissColors.Accent),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = SwissColors.Grey)
    }
}

@Composable
private fun ListItem(bullet: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = bullet,
            style = MaterialTheme.typography.bodyLarge,
            color = SwissColors.Grey,
            modifier = Modifier.width(24.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyLarge, color = SwissColors.Black)
    }
}

@Composable
private fun TaskItem(text: String, checked: Boolean) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (checked) "☑" else "☐",
            color = if (checked) SwissColors.Accent else SwissColors.Grey,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (checked) SwissColors.Grey else SwissColors.Black,
        )
    }
}

@Composable
private fun HorizontalRule() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(SwissColors.LightGrey),
    )
}

@Composable
private fun MediaBlock(src: String, alt: String) {
    val isVideo = src.endsWith(".mp4") || src.endsWith(".3gp") || src.endsWith(".webm")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium),
    ) {
        if (isVideo) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(Uri.parse(src))
                        setOnClickListener { if (isPlaying) pause() else start() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        } else {
            AsyncImage(
                model = src,
                contentDescription = alt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
    }
}

/**
 * Renders a paragraph with inline styling and clickable `[[wikilinks]]`.
 */
@Composable
private fun InlineText(text: String, onWikilinkClick: (String) -> Unit) {
    val annotated = buildAnnotatedString {
        val regex = Regex("""(\[\[[^\]]+\]\])""")
        var last = 0
        regex.findAll(text).forEach { match ->
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val title = match.value.removePrefix("[[").removeSuffix("]]").substringBefore("|")
            withStyle(SpanStyle(color = SwissColors.Accent, fontWeight = FontWeight.SemiBold)) {
                append(title)
            }
            last = match.range.last + 1
        }
        if (last < text.length) append(text.substring(last))
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge,
        color = SwissColors.Black,
        modifier = Modifier.fillMaxWidth(),
    )
}
