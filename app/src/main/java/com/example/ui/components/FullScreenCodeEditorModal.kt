package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SkyGlow

/**
 * Clean, distraction-free Termux-style terminal editor for Android.
 * Pure terminal experience: code editing only, without noisy buttons or captions.
 */
@Composable
fun FullScreenCodeEditorModal(
    code: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val lineCount = remember(code) {
        if (code.isEmpty()) 1 else code.lines().size
    }

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
                                onCodeChange(code + insertText)
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

            // Pure Terminal Editor Area with Gutter & Syntax Highlighting
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF000000))
            ) {
                // Termux Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF05080E))
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..maxOf(lineCount, 1)) {
                        Text(
                            text = "$i",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF334155)
                            )
                        )
                    }
                }

                // Clean Termux Code Surface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
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
