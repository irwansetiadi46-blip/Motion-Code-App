package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

object JsSyntaxHighlighter : VisualTransformation {

    // Theme Colors for Syntax Highlighting (Dark Cyber / VS Code Modern)
    private val KeywordColor = Color(0xFF38BDF8)       // Electric Sky Blue
    private val BuiltinColor = Color(0xFFC084FC)       // Electric Lavender / Violet
    private val CanvasMethodColor = Color(0xFFFBBF24)  // Amber Gold
    private val StringColor = Color(0xFF34D399)        // Emerald Green
    private val NumberColor = Color(0xFFFB7185)        // Soft Coral / Rose
    private val CommentColor = Color(0xFF64748B)       // Muted Slate
    private val OperatorColor = Color(0xFF94A3B8)      // Silver
    private val FunctionCallColor = Color(0xFF67E8F9)  // Cyan

    // Regular Expressions
    private val CommentPattern = Pattern.compile("(//.*)|(/\\*[\\s\\S]*?\\*/)")
    private val StringPattern = Pattern.compile("(\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'|`[^`\\\\]*(?:\\\\.[^`\\\\]*)*`)")
    private val KeywordPattern = Pattern.compile("\\b(const|let|var|function|return|if|else|for|while|do|break|continue|switch|case|default|new|try|catch|finally|throw|class|extends|super|import|export|from|async|await|typeof|instanceof|void|delete|in|of|true|false|null|undefined)\\b")
    private val BuiltinPattern = Pattern.compile("\\b(document|window|console|Math|Date|Array|Object|String|Number|Boolean|Map|Set|Promise|JSON|requestAnimationFrame|cancelAnimationFrame|addEventListener|removeEventListener)\\b")
    private val CanvasMethodPattern = Pattern.compile("\\b(getContext|fillRect|strokeRect|clearRect|beginPath|closePath|moveTo|lineTo|arc|arcTo|stroke|fill|rect|ellipse|createLinearGradient|createRadialGradient|addColorStop|save|restore|translate|rotate|scale|drawImage|measureText|fillText|strokeText|fillStyle|strokeStyle|lineWidth|lineCap|lineJoin|shadowColor|shadowBlur|font|globalAlpha|width|height|innerWidth|innerHeight|c|ctx|W|H)\\b")
    private val NumberPattern = Pattern.compile("\\b(\\d+(\\.\\d+)?|0x[0-9a-fA-F]+)\\b")
    private val FunctionCallPattern = Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)(?=\\s*\\()")

    fun highlight(text: String): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        val length = text.length

        // 1. Function calls
        val funcMatcher = FunctionCallPattern.matcher(text)
        while (funcMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = FunctionCallColor),
                funcMatcher.start(1),
                funcMatcher.end(1)
            )
        }

        // 2. Numbers
        val numMatcher = NumberPattern.matcher(text)
        while (numMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = NumberColor),
                numMatcher.start(),
                numMatcher.end()
            )
        }

        // 3. Canvas Methods & Properties
        val canvasMatcher = CanvasMethodPattern.matcher(text)
        while (canvasMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = CanvasMethodColor, fontWeight = FontWeight.SemiBold),
                canvasMatcher.start(),
                canvasMatcher.end()
            )
        }

        // 4. Built-in objects
        val builtinMatcher = BuiltinPattern.matcher(text)
        while (builtinMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = BuiltinColor, fontWeight = FontWeight.SemiBold),
                builtinMatcher.start(),
                builtinMatcher.end()
            )
        }

        // 5. Keywords
        val kwMatcher = KeywordPattern.matcher(text)
        while (kwMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold),
                kwMatcher.start(),
                kwMatcher.end()
            )
        }

        // 6. Strings (higher priority over keywords/numbers inside string)
        val strMatcher = StringPattern.matcher(text)
        while (strMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = StringColor),
                strMatcher.start(),
                strMatcher.end()
            )
        }

        // 7. Comments (highest priority to override everything inside comment)
        val commentMatcher = CommentPattern.matcher(text)
        while (commentMatcher.find()) {
            builder.addStyle(
                SpanStyle(color = CommentColor),
                commentMatcher.start(),
                commentMatcher.end()
            )
        }

        return builder.toAnnotatedString()
    }

    private var lastText: String? = null
    private var lastResult: AnnotatedString? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.length > 20000) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        if (raw == lastText && lastResult != null) {
            return TransformedText(lastResult!!, OffsetMapping.Identity)
        }
        val result = highlight(raw)
        lastText = raw
        lastResult = result
        return TransformedText(result, OffsetMapping.Identity)
    }
}
