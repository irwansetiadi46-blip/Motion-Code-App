package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FullscreenExit
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import com.example.ui.theme.TerminalBg
import kotlinx.coroutines.launch

@Composable
fun FullScreenCodeEditorModal(
    code: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Initialize cursor at position 0 to avoid jumping to the end on first click
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(code, TextRange(0)))
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

    val editorVerticalScrollState = rememberScrollState()
    val editorHorizontalScrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var editorHeightPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottom > 0

    // Snippets Toolbar matching the mini Terminal exactly
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

    // Smart Cursor Scroll: pointer selalu terlihat tanpa lompatan scroll jauh
    fun keepCursorVisible(cursorPos: Int, animated: Boolean = true) {
        val layout = textLayoutResult ?: return
        if (editorHeightPx <= 0) return

        val clampedOffset = cursorPos.coerceIn(0, textFieldValue.text.length)
        val lineIndex = layout.getLineForOffset(clampedOffset)
        val lineTopPx = layout.getLineTop(lineIndex)
        val lineBottomPx = layout.getLineBottom(lineIndex)
        val lineHeightPx = (lineBottomPx - lineTopPx).coerceAtLeast(26f)

        val currentScrollY = editorVerticalScrollState.value.toFloat()
        val visibleHeight = editorHeightPx.toFloat()

        // 2 lines comfort clearance
        val topMarginPx = lineHeightPx * 1.5f
        val bottomMarginPx = lineHeightPx * 2.5f

        val cursorRelBottom = lineBottomPx - currentScrollY
        val cursorRelTop = lineTopPx - currentScrollY

        if (cursorRelBottom > (visibleHeight - bottomMarginPx)) {
            // Kursor tertutup keyboard atau mendekati dasar: scroll ke bawah secukupnya
            val targetScroll = (lineBottomPx - visibleHeight + bottomMarginPx).coerceAtLeast(0f)
            coroutineScope.launch {
                if (animated) {
                    editorVerticalScrollState.animateScrollTo(targetScroll.toInt())
                } else {
                    editorVerticalScrollState.scrollTo(targetScroll.toInt())
                }
            }
        } else if (cursorRelTop < topMarginPx) {
            // Kursor di atas area terlihat: scroll ke atas secukupnya
            val targetScroll = (lineTopPx - topMarginPx).coerceAtLeast(0f)
            coroutineScope.launch {
                if (animated) {
                    editorVerticalScrollState.animateScrollTo(targetScroll.toInt())
                } else {
                    editorVerticalScrollState.scrollTo(targetScroll.toInt())
                }
            }
        }
        // Jika kursor sudah berada di area terlihat, TIDAK PERLU SCROLL sama sekali (tanpa lompatan jauh)
    }

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            keepCursorVisible(textFieldValue.selection.start, animated = true)
        }
    }

    BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Tapping anywhere outside input fields clears pointer and hides keyboard
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                }
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalBg),
                border = BorderStroke(1.5.dp, PurpleBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Terminal Header: Same design, texts, and colors as Mini Terminal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Green dot indicator & Text "Terminal"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
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

                        // Header Action Buttons matching Mini Terminal
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Exit Fullscreen mode button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Keluar Fullscreen",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Salin Kode
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(textFieldValue.text))
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

                            // Tempel Kode
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrEmpty()) {
                                        textFieldValue = TextFieldValue(clip, TextRange(clip.length))
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

                            // Bersihkan Editor
                            IconButton(
                                onClick = {
                                    textFieldValue = TextFieldValue("", TextRange(0))
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

                    // Quick Snippets Toolbar matching Mini Terminal exactly
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
                                border = BorderStroke(1.dp, Color(0xFF334155))
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

                    // Terminal Code Text Area with Smart Cursor Positioning (2 lines above keyboard)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TerminalBg)
                            .onGloballyPositioned { coordinates ->
                                val newH = coordinates.size.height
                                if (editorHeightPx != newH) {
                                    editorHeightPx = newH
                                    if (isKeyboardVisible) {
                                        keepCursorVisible(textFieldValue.selection.start, animated = true)
                                    }
                                }
                            }
                            .verticalScroll(editorVerticalScrollState)
                            .horizontalScroll(editorHorizontalScrollState)
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

                            // Tap pada area kosong di bawah kode: buka keyboard dan arahkan kursor ke akhir
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp)
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
    }
}
