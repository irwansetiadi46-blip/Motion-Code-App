package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark futuristic slate & cyber tones
val DarkBackground = Color(0xFF070B19)
val DarkSurface = Color(0xCC0F172A)
val DarkSurfaceElevated = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkBorder = Color(0xFF334155)

// Accent and Brand Colors
val ElectricCyan = Color(0xFF00E5FF)
val ElectricBlue = Color(0xFF0284C7)
val SkyGlow = Color(0xFF38BDF8)
val NeonPurple = Color(0xFF818CF8)
val PurpleBorder = Color(0xFFA855F7)
val CyberPink = Color(0xFFF43F5E)
val MatrixGreen = Color(0xFF10B981)
val AmberGlow = Color(0xFFF59E0B)

// Text and muted
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Terminal & Editor colors
val TerminalBg = Color(0xFF090D16)
val TerminalGutter = Color(0xFF070B12)
val TerminalGutterText = Color(0xFF475569)
val TerminalBorder = Color(0xFF1E293B)

// Aesthetics Main Background Gradient (Navy blue -> Purple -> Brighter luminous gradient at bottom)
val MainBackgroundGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF060A18), // Deep navy
        Color(0xFF0B1230), // Dark royal navy
        Color(0xFF1A1242), // Aesthetic violet/purple
        Color(0xFF162352)  // Lighter glowing navy-indigo at bottom
    )
)

// Glassmorphism Header (Sky blue to purple gradient with translucent glass)
val GlassHeaderBackground = Brush.horizontalGradient(
    listOf(
        Color(0x380284C7), // Sky blue tint
        Color(0x388B5CF6), // Purple tint
        Color(0x2838BDF8)  // Sky glow tint
    )
)

val GlassHeaderBorder = Brush.horizontalGradient(
    listOf(
        Color(0x6638BDF8),
        Color(0x77C084FC),
        Color(0x4438BDF8)
    )
)

// Glassmorphism Bottom Navigation Bar
val GlassNavBackground = Brush.verticalGradient(
    listOf(
        Color(0xD90A102A),
        Color(0xEE140E34)
    )
)

val GlassNavBorder = Brush.horizontalGradient(
    listOf(
        Color(0x5538BDF8),
        Color(0x66A855F7),
        Color(0x4438BDF8)
    )
)
