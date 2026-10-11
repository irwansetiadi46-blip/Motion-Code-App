package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QualityBitrate
import com.example.model.RenderConfig
import com.example.model.VideoResolution
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow

/**
 * Container "Export Configuration" yang menggabungkan semua opsi export:
 * Resolusi, Durasi, FPS, dan Bitrate ke dalam format Dropdown.
 */
@Composable
fun ExportConfigurationCard(
    renderConfig: RenderConfig,
    onResolutionChange: (VideoResolution) -> Unit,
    onDurationChange: (Int) -> Unit,
    onFpsChange: (Int) -> Unit,
    onQualityChange: (QualityBitrate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.5.dp, PurpleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Container: Export Configuration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x330284C7),
                    border = BorderStroke(1.dp, Color(0x6638BDF8)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = SkyGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Export Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Konfigurasi resolusi, durasi, frame rate, dan bitrate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 1. Dropdown Resolusi Video
            ConfigurationDropdown(
                label = "Resolusi Video",
                currentValue = renderConfig.resolution.label,
                icon = Icons.Default.AspectRatio,
                badge = "${renderConfig.resolution.width}x${renderConfig.resolution.height}"
            ) { closeMenu ->
                VideoResolution.ALL.forEach { res ->
                    val isSelected = res.width == renderConfig.resolution.width &&
                            res.height == renderConfig.resolution.height
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.label,
                                    color = if (isSelected) SkyGlow else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = res.category.displayName.take(8),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        onClick = {
                            onResolutionChange(res)
                            closeMenu()
                        }
                    )
                }
            }

            // 2. Dropdown Durasi Video
            val durationOptions = listOf(3, 5, 8, 10, 12, 15, 20, 25, 30)
            ConfigurationDropdown(
                label = "Durasi Video",
                currentValue = "${renderConfig.durationSeconds} Detik",
                icon = Icons.Default.Timer,
                badge = "${renderConfig.totalFrames} Frames"
            ) { closeMenu ->
                durationOptions.forEach { dur ->
                    val isSelected = dur == renderConfig.durationSeconds
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$dur Detik (${dur * renderConfig.fps} total frames)",
                                color = if (isSelected) SkyGlow else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onDurationChange(dur)
                            closeMenu()
                        }
                    )
                }
            }

            // 3. Dropdown FPS (Frame Rate)
            val fpsOptions = listOf(24, 30, 60)
            ConfigurationDropdown(
                label = "Frame Rate (FPS)",
                currentValue = "${renderConfig.fps} FPS",
                icon = Icons.Default.Speed,
                badge = when (renderConfig.fps) {
                    24 -> "Cinematic"
                    30 -> "Standard"
                    else -> "Smooth 60"
                }
            ) { closeMenu ->
                fpsOptions.forEach { fps ->
                    val isSelected = fps == renderConfig.fps
                    val desc = when (fps) {
                        24 -> "Cinematic Film (24 FPS)"
                        30 -> "Web / Media Sosial (30 FPS)"
                        60 -> "Ultra Smooth Motion (60 FPS)"
                        else -> "$fps FPS"
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = desc,
                                color = if (isSelected) SkyGlow else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onFpsChange(fps)
                            closeMenu()
                        }
                    )
                }
            }

            // 4. Dropdown Bitrate / Kualitas Output
            ConfigurationDropdown(
                label = "Bitrate / Kualitas Output",
                currentValue = renderConfig.quality.label,
                icon = Icons.Default.VideoSettings,
                badge = "${renderConfig.safeBitrateBps / 1_000_000} Mbps"
            ) { closeMenu ->
                QualityBitrate.ALL.forEach { q ->
                    val isSelected = q.bitrateBps == renderConfig.quality.bitrateBps
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = q.label,
                                color = if (isSelected) SkyGlow else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onQualityChange(q)
                            closeMenu()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationDropdown(
    label: String,
    currentValue: String,
    icon: ImageVector,
    badge: String? = null,
    menuContent: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrowRotation")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SkyGlow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0)
                    )
                )
            }

            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0x330284C7)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // Clickable Dropdown Trigger Box
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF131D3B),
                border = BorderStroke(1.dp, if (expanded) SkyGlow else DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih",
                        tint = SkyGlow,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF0F172A))
            ) {
                menuContent { expanded = false }
            }
        }
    }
}
