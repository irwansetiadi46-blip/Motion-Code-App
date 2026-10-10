package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoMetadata
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SkyGlow
import com.example.util.CsvExporter
import com.example.util.MetadataValidator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockMetadataCard(
    metadata: VideoMetadata,
    onMetadataChange: (VideoMetadata) -> Unit,
    onGenerateAi: () -> Unit,
    onDownloadCsv: () -> Unit,
    onShareCsv: () -> Unit,
    isGenerating: Boolean,
    videoFilename: String = "code_motion_video.mp4",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val validation = remember(metadata) { MetadataValidator.validate(metadata) }
    var inputKeyword by remember { mutableStateOf("") }

    val glassBorderBrush = Brush.horizontalGradient(
        listOf(
            Color(0x5538BDF8),
            Color(0x66A855F7),
            Color(0x4438BDF8)
        )
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x990C132E)),
        border = BorderStroke(1.2.dp, glassBorderBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: "Metadata"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x330284C7),
                    border = BorderStroke(1.dp, Color(0x4438BDF8)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SkyGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Metadata",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Tombol Generate Metadata mepet kiri & Tombol Clear All di sebelah kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onGenerateAi,
                    enabled = !isGenerating,
                    modifier = Modifier.height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generate Metadata",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        onMetadataChange(VideoMetadata(title = "", description = "", keywords = ""))
                    },
                    modifier = Modifier.height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
                    border = BorderStroke(1.dp, Color(0x66F43F5E)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear All Metadata",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFF43F5E)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF43F5E)
                        )
                    )
                }
            }

            // Field 1: Title (English)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Title (English)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${validation.titleWordCount} kata | ${validation.titleLength}/100",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (validation.isTitleValid) MatrixGreen else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                        IconButton(
                            onClick = {
                                CsvExporter.copyToClipboard(context, "Title", metadata.title)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Title",
                                tint = SkyGlow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = metadata.title,
                    onValueChange = { onMetadataChange(metadata.copy(title = it)) },
                    placeholder = {
                        Text("Futuristic code animation motion background", color = Color(0xFF64748B), fontSize = 13.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x66080E24),
                        unfocusedContainerColor = Color(0x44080E24),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = SkyGlow,
                        unfocusedBorderColor = DarkBorder.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = false,
                    maxLines = 2
                )
            }

            // Field 2: Description (English)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Description (English)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${validation.descLength}/150 char",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (validation.isDescValid) MatrixGreen else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                        IconButton(
                            onClick = {
                                CsvExporter.copyToClipboard(context, "Description", metadata.description)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Description",
                                tint = SkyGlow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = metadata.description,
                    onValueChange = { onMetadataChange(metadata.copy(description = it)) },
                    placeholder = {
                        Text("Programming syntax animation rendered seamlessly in digital space.", color = Color(0xFF64748B), fontSize = 13.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x66080E24),
                        unfocusedContainerColor = Color(0x44080E24),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = SkyGlow,
                        unfocusedBorderColor = DarkBorder.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3
                )
            }

            // Field 3: Keywords - Kolom input dengan tombol Add di sebelah kanan & Clear Keywords
            val currentKeywordsList = remember(metadata.keywords) { metadata.parseKeywordsList() }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keywords (${currentKeywordsList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${validation.keywordCount}/49 (min 30)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (validation.isKeywordsValid) MatrixGreen else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )

                        // Tombol Copy Keywords
                        IconButton(
                            onClick = {
                                CsvExporter.copyToClipboard(context, "Keywords", metadata.cleanKeywordsString())
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Keywords",
                                tint = SkyGlow,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Tombol Clear Keywords
                        Surface(
                            onClick = {
                                onMetadataChange(metadata.copy(keywords = ""))
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33F43F5E),
                            border = BorderStroke(1.dp, Color(0x55F43F5E))
                        ) {
                            Text(
                                text = "Clear",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF43F5E),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Kolom input keyword dengan tombol Add seperti API key
                OutlinedTextField(
                    value = inputKeyword,
                    onValueChange = { inputKeyword = it },
                    placeholder = {
                        Text(
                            text = "Ketik keyword (misal: technology, coding)...",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x66080E24),
                        unfocusedContainerColor = Color(0x44080E24),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = SkyGlow,
                        unfocusedBorderColor = DarkBorder.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Button(
                            onClick = {
                                val trimmed = inputKeyword.trim().trim(',', ' ')
                                if (trimmed.isNotEmpty()) {
                                    val newTags = trimmed.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                    val combined = (currentKeywordsList + newTags).distinct()
                                    onMetadataChange(metadata.copy(keywords = combined.joinToString(", ")))
                                    inputKeyword = ""
                                }
                            },
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Add",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                // Keyword Tags di FlowRow
                if (currentKeywordsList.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0x44071120),
                        border = BorderStroke(1.dp, Color(0x33334155))
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentKeywordsList.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x551E293B),
                                    border = BorderStroke(1.dp, Color(0x44475569))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                color = Color(0xFFE2E8F0)
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus $tag",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable {
                                                    val updatedList = currentKeywordsList.filter { it != tag }
                                                    onMetadataChange(metadata.copy(keywords = updatedList.joinToString(", ")))
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Actions Row - Clean & Minimal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownloadCsv,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download CSV",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        val fullText = "Title:\n${metadata.title}\n\nDescription:\n${metadata.description}\n\nKeywords:\n${metadata.cleanKeywordsString()}"
                        CsvExporter.copyToClipboard(context, "Semua Metadata", fullText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShareCsv,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan", fontSize = 12.sp)
                }
            }
        }
    }
}
