package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.ui.theme.SkyGlow

/**
 * Clean, distraction-free Termux-style terminal editor for Android.
 * Pure terminal experience: code editing only, cursor-aware snippet insertion.
 * Keyboard behaves deterministically: tapping text toggles/shows keyboard,
 * scrolling never triggers or dismisses the soft keyboard.
 * Line gutter is synchronized with text and allows scrolling 10 lines past EOF.
 */
@Composable
fun FullScreenCodeEditorModal(
    code: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
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

    val editorVerticalScrollState = rememberScrollState()
    val editorHorizontalScrollState = rememberScrollState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isKeyboardActive by remember { mutableStateOf(false) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    // Termux extra keys bar (matching Termux mobile keyboard toolbar)
    val termuxKeys = listOf(
        "ESC" to "",
        "TAB" to "  ",
        "{" to "{",
        "}" to "}",
        "(" to "(",
        ")" to ")",
        "[" to "[",
        "]" to "]",
        ";" to ";",
        "=" to " = ",
        "W" to "W",
        "H" to "H",
        "const" to "const ",
        "ctx." to "ctx.",
        "Math." to "Math.",
        "loop" to "function loop(t) {\n  requestAnimationFrame(loop);\n}\nrequestAnimationFrame(loop);"
    )

    // Android back gesture automatically saves and exits
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000)) // Pure Termux black
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Minimal Termux Prompt Header (Clean Termux Shell banner)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "root@termux",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF10B981) // Termux Green
                        )
                    )
                    Text(
                        text = ":",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "~/animation.js",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8) // Termux Blue
                        )
                    )
                    Text(
                        text = "$ nano",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                // Minimal exit indicator button (ESC)
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "[ESC]",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            // Termux Extra-Keys toolbar (Above software keyboard)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0E17))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                termuxKeys.forEach { (label, insertText) ->
                    Surface(
                        onClick = {
                            if (label == "ESC") {
                                onDismiss()
                            } else {
                                val currentVal = textFieldValue
                                val sel = currentVal.selection
                                val newText = currentVal.text.replaceRange(sel.start, sel.end, insertText)
                                val newCursor = sel.start + insertText.length
                                textFieldValue = TextFieldValue(newText, TextRange(newCursor))
                                onCodeChange(newText)
                            }
                        },
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF161F30),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24324D))
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (label == "ESC") FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (label == "ESC") Color(0xFFF43F5E) else SkyGlow
                            )
                        )
                    }
                }
            }

            // Pure Terminal Editor Area with Synchronized Gutter & Syntax Highlighting
            // Scroll behavior: Scrolling does NOT toggle/show/hide soft keyboard.
            // Tap behavior: Only a tap explicitly toggles or shows/hides soft keyboard.
            // Extra scroll space: Allows scrolling 10 lines beyond the end of code.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF000000))
            ) {
                // Termux Line Numbers Gutter (Synced with editorVerticalScrollState)
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF05080E))
                        .verticalScroll(editorVerticalScrollState)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val maxLineNumber = maxOf(lineCount, 1)
                    for (i in 1..maxLineNumber) {
                        Text(
                            text = "$i",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF334155)
                            )
                        )
                    }
                    // 10 baris padding kosong di bawah agar gutter selaras saat user scroll melebihi baris terakhir
                    for (i in 1..10) {
                        Text(
                            text = "",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Clean Termux Code Surface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(editorVerticalScrollState)
                        .horizontalScroll(editorHorizontalScrollState)
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    // Tap hanya pada teks yang memicu keyboard muncul / tersembunyi
                                    if (isKeyboardActive) {
                                        // Jika keyboard sedang muncul dan di-tap lagi, toggle hide
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        isKeyboardActive = false
                                    } else {
                                        // Jika keyboard belum muncul, tap memunculkan keyboard dan memindahkan cursor
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                        isKeyboardActive = true

                                        textLayoutResult?.let { layout ->
                                            val offset = layout.getOffsetForPosition(tapOffset)
                                            textFieldValue = textFieldValue.copy(
                                                selection = TextRange(offset.coerceIn(0, textFieldValue.text.length))
                                            )
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = {
                                textFieldValue = it
                                onCodeChange(it.text)
                            },
                            onTextLayout = { layoutResult ->
                                textLayoutResult = layoutResult
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFFE2E8F0)
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            visualTransformation = JsSyntaxHighlighter,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrect = false
                            )
                        )

                        // 10 baris kosong ekstra di bawah teks code (20.sp * 10 = 200.dp sepadan)
                        Column {
                            repeat(10) {
                                Text(
                                    text = " ",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.5.sp,
                                        lineHeight = 20.sp,
                                        color = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
