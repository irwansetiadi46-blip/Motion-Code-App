package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioCategory
import com.example.model.VideoResolution
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow

@Composable
fun ResolutionSelector(
    selectedResolution: VideoResolution,
    onResolutionSelected: (VideoResolution) -> Unit,
    modifier: Modifier = Modifier
) {
    val aspectPresets = listOf(
        "16:9" to AspectRatioCategory.LANDSCAPE,
        "9:16" to AspectRatioCategory.VERTICAL_9_16,
        "4:5" to AspectRatioCategory.VERTICAL_4_5,
        "3:4" to AspectRatioCategory.VERTICAL_3_4,
        "1:1" to AspectRatioCategory.SQUARE
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.5.dp, PurpleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Format & Aspect Ratio",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Aspect Presets Quick Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspectPresets.forEach { (label, category) ->
                    val isActive = selectedResolution.category == category
                    Surface(
                        onClick = {
                            val is4k = selectedResolution.label.contains("4K")
                            val is2k = selectedResolution.label.contains("2K")
                            val isHd = selectedResolution.label.startsWith("HD") || selectedResolution.label.contains(" 720")
                            val defaultForCategory = VideoResolution.ALL.filter { it.category == category }.firstOrNull { target ->
                                when {
                                    is4k -> target.label.contains("4K")
                                    is2k -> target.label.contains("2K")
                                    isHd -> target.label.startsWith("HD") || target.label.contains(" 720")
                                    else -> target.label.contains("Full HD")
                                }
                            } ?: VideoResolution.ALL.firstOrNull { it.category == category && it.isDefault }
                              ?: VideoResolution.ALL.firstOrNull { it.category == category }

                            if (defaultForCategory != null) {
                                onResolutionSelected(defaultForCategory)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActive) ElectricBlue else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isActive) SkyGlow else DarkBorder)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) Color.White else Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resolutions within the category
            Text(
                text = "Pilihan Resolusi:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))

            val currentCategoryResolutions = VideoResolution.ALL.filter { it.category == selectedResolution.category }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                currentCategoryResolutions.forEach { res ->
                    val isSelected = res == selectedResolution
                    Surface(
                        onClick = { onResolutionSelected(res) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF0C2A44) else Color(0xFF131B2E),
                        border = BorderStroke(1.dp, if (isSelected) SkyGlow else DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = res.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                                )
                            )
                            Text(
                                text = "${res.width}×${res.height}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) SkyGlow else Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
