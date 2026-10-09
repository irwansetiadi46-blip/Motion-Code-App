package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalGutter
import com.example.ui.theme.TerminalGutterText

@Composable
fun CodeTerminalEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    onApplyCode: () -> Unit = {},
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(code, TextRange(code.length)))
    }

    LaunchedEffect(code) {
        if (code != textFieldValue.text) {
            val curPos = textFieldValue.selection.start.coerceIn(0, code.length)
            textFieldValue = textFieldValue.copy(
                text = code,
                selection = TextRange(curPos)
            )
        }
    }

    val lineCount = remember(textFieldValue.text) {
        if (textFieldValue.text.isEmpty()) 1 else textFieldValue.text.lines().size
    }

    val snippets = listOf(
        "const " to "const ",
        "let " to "let ",
        "function" to "function () {\n  \n}",
        "ctx." to "ctx.",
        "Math." to "Math.",
        "W, H" to "W, H",
        "{ }" to "{\n  \n}",
        "( )" to "( )",
        "[ ]" to "[ ]",
        ";" to ";",
        "=>" to " => ",
        "requestAnimationFrame" to "requestAnimationFrame(loop);",
        "hsla" to "hsla(200, 100%, 60%, 0.8)",
        "rgba" to "rgba(0, 0, 0, 0.2)"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Terminal Header with macOS dots and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dots & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "animation.js",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                // Header Action Buttons (Edit Fullscreen, Copy, Paste, Clear)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tombol Edit (Fullscreen Terminal Mode) di samping kiri icon copy
                    IconButton(
                        onClick = onOpenFullscreen,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Buka Mode Terminal Editor Full Screen",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "Kode disalin", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Kode",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrEmpty()) {
                                onCodeChange(clip)
                                Toast.makeText(context, "Kode ditempel", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Tempel Kode",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onCodeChange("")
                            Toast.makeText(context, "Editor dibersihkan", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Bersihkan",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Quick Snippets Toolbar for mobile coding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                snippets.forEach { (label, insertText) ->
                    Surface(
                        onClick = {
                            val currentVal = textFieldValue
                            val sel = currentVal.selection
                            val newText = currentVal.text.replaceRange(sel.start, sel.end, insertText)
                            val newCursor = sel.start + insertText.length
                            textFieldValue = TextFieldValue(newText, TextRange(newCursor))
                            onCodeChange(newText)
                        },
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SkyGlow
                            )
                        )
                    }
                }
            }

            // Line numbers & Code text area with Syntax Highlighting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(TerminalBg)
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .background(TerminalGutter)
                        .padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val maxLinesShown = minOf(lineCount, 60)
                    for (i in 1..maxLinesShown) {
                        Text(
                            text = "$i",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 18.sp,
                                color = TerminalGutterText
                            )
                        )
                    }
                }

                // Editor Text Field with live Syntax Highlighting
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "// Tulis atau paste kode animasi canvas di sini...\n// Contoh: c.getContext('2d');\n// Wajib ada loop animasi.",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF475569)
                            )
                        )
                    }

                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                            onCodeChange(it.text)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        ),
                        cursorBrush = SolidColor(ElectricCyan),
                        visualTransformation = JsSyntaxHighlighter,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrect = false
                        )
                    )
                }
            }

        }
    }
}
