package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.launch
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
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import com.example.ui.theme.TerminalBg

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
        mutableStateOf(TextFieldValue(code, TextRange(0)))
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var editorHeightPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottom > 0

    val miniVerticalScroll = rememberScrollState()
    val miniHorizontalScroll = rememberScrollState()

    // Smart Cursor Scroll: pointer selalu terlihat tanpa lompatan scroll jauh
    fun keepCursorVisible(cursorPos: Int, animated: Boolean = true) {
        val layout = textLayoutResult ?: return
        if (editorHeightPx <= 0) return

        val clampedOffset = cursorPos.coerceIn(0, textFieldValue.text.length)
        val lineIndex = layout.getLineForOffset(clampedOffset)
        val lineTopPx = layout.getLineTop(lineIndex)
        val lineBottomPx = layout.getLineBottom(lineIndex)
        val lineHeightPx = (lineBottomPx - lineTopPx).coerceAtLeast(24f)

        val currentScrollY = miniVerticalScroll.value.toFloat()
        val visibleHeight = editorHeightPx.toFloat()

        val topMarginPx = lineHeightPx * 1.5f
        val bottomMarginPx = lineHeightPx * 2.5f

        val cursorRelBottom = lineBottomPx - currentScrollY
        val cursorRelTop = lineTopPx - currentScrollY

        if (cursorRelBottom > (visibleHeight - bottomMarginPx)) {
            val targetScroll = (lineBottomPx - visibleHeight + bottomMarginPx).coerceAtLeast(0f)
            coroutineScope.launch {
                if (animated) {
                    miniVerticalScroll.animateScrollTo(targetScroll.toInt())
                } else {
                    miniVerticalScroll.scrollTo(targetScroll.toInt())
                }
            }
        } else if (cursorRelTop < topMarginPx) {
            val targetScroll = (lineTopPx - topMarginPx).coerceAtLeast(0f)
            coroutineScope.launch {
                if (animated) {
                    miniVerticalScroll.animateScrollTo(targetScroll.toInt())
                } else {
                    miniVerticalScroll.scrollTo(targetScroll.toInt())
                }
            }
        }
    }

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            keepCursorVisible(textFieldValue.selection.start, animated = true)
        }
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
            // Terminal Header: "Terminal" dengan indikator dot hijau
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indikator dot hijau & Text "Terminal"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)) // Green dot indicator
                    )
                    Text(
                        text = "Terminal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Header Action Buttons (Edit Fullscreen, Copy, Paste, Clear)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tombol Edit (Fullscreen Terminal Mode)
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

            // Code text area without line numbers gutter (clean & spacious)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(TerminalBg)
                    .onGloballyPositioned { coords ->
                        val newH = coords.size.height
                        if (editorHeightPx != newH) {
                            editorHeightPx = newH
                            if (isKeyboardVisible) {
                                keepCursorVisible(textFieldValue.selection.start, animated = true)
                            }
                        }
                    }
                    .verticalScroll(miniVerticalScroll)
                    .horizontalScroll(miniHorizontalScroll)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        if (event.changes.any { it.changedToDown() }) {
                                            focusRequester.requestFocus()
                                            keyboardController?.show()
                                        }
                                    }
                                }
                            }
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
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onCodeChange(newValue.text)
                                keepCursorVisible(newValue.selection.start, animated = true)
                            },
                            onTextLayout = { layout ->
                                textLayoutResult = layout
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
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

                    // Area kosong di bawah kode: jika di-tap langsung memunculkan keyboard dan kursor di akhir
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                                val endPos = textFieldValue.text.length
                                textFieldValue = textFieldValue.copy(selection = TextRange(endPos))
                                keepCursorVisible(endPos, animated = true)
                            }
                    )
                }
            }
        }
    }
}
