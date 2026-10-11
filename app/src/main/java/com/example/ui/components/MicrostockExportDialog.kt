package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MicrostockPlatform
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SkyGlow

@Composable
fun MicrostockExportDialog(
    onDismiss: () -> Unit,
    onSave: (selectedPlatforms: Set<MicrostockPlatform>) -> Unit
) {
    // Default: Adobe Stock & Shutterstock tercentang
    var selectedPlatforms by remember {
        mutableStateOf(setOf(MicrostockPlatform.ADOBE_STOCK, MicrostockPlatform.SHUTTERSTOCK))
    }

    val isAllSelected = selectedPlatforms.size == MicrostockPlatform.entries.size

    val dialogBorderBrush = Brush.linearGradient(
        listOf(
            Color(0x8838BDF8),
            Color(0x99A855F7),
            Color(0x6638BDF8)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00B112C)),
                border = BorderStroke(1.5.dp, dialogBorderBrush)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x330284C7),
                        border = BorderStroke(1.dp, Color(0x6638BDF8)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = SkyGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Download CSV Microstock",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Pilih format platform microstock yang sesuai:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Format Card 2 Kolom
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Baris 1 (2 Kolom): Adobe Stock & Shutterstock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Kolom 1: Adobe Stock Card
                        MicrostockSelectionCard(
                            title = "Adobe Stock",
                            subtitle = "Title, Keywords, Category",
                            accentColor = Color(0xFFFF2A2A),
                            badgeText = "ADOBE",
                            isSelected = selectedPlatforms.contains(MicrostockPlatform.ADOBE_STOCK),
                            onClick = {
                                selectedPlatforms = if (selectedPlatforms.contains(MicrostockPlatform.ADOBE_STOCK)) {
                                    selectedPlatforms - MicrostockPlatform.ADOBE_STOCK
                                } else {
                                    selectedPlatforms + MicrostockPlatform.ADOBE_STOCK
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Kolom 2: Shutterstock Card
                        MicrostockSelectionCard(
                            title = "Shutterstock",
                            subtitle = "Description, Keywords, Categories",
                            accentColor = Color(0xFFFF5252),
                            badgeText = "SHUTTER",
                            isSelected = selectedPlatforms.contains(MicrostockPlatform.SHUTTERSTOCK),
                            onClick = {
                                selectedPlatforms = if (selectedPlatforms.contains(MicrostockPlatform.SHUTTERSTOCK)) {
                                    selectedPlatforms - MicrostockPlatform.SHUTTERSTOCK
                                } else {
                                    selectedPlatforms + MicrostockPlatform.SHUTTERSTOCK
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Baris 2 (2 Kolom): Vecteezy & Pilih Semua
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Kolom 1: Vecteezy Card
                        MicrostockSelectionCard(
                            title = "Vecteezy",
                            subtitle = "Title, Description, License",
                            accentColor = Color(0xFFFF9E0B),
                            badgeText = "VECTEEZY",
                            isSelected = selectedPlatforms.contains(MicrostockPlatform.VECTEEZY),
                            onClick = {
                                selectedPlatforms = if (selectedPlatforms.contains(MicrostockPlatform.VECTEEZY)) {
                                    selectedPlatforms - MicrostockPlatform.VECTEEZY
                                } else {
                                    selectedPlatforms + MicrostockPlatform.VECTEEZY
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Kolom 2: Pilih Semua
                        MicrostockSelectionCard(
                            title = "Pilih Semua",
                            subtitle = "Simpan ke 3 format sekaligus",
                            accentColor = ElectricCyan,
                            badgeText = "ALL",
                            isSelected = isAllSelected,
                            onClick = {
                                selectedPlatforms = if (isAllSelected) {
                                    emptySet()
                                } else {
                                    MicrostockPlatform.entries.toSet()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Info teks catatan format
                Text(
                    text = "File CSV akan disimpan otomatis di folder: Download/Code Motion Video",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                )

                // Tombol Batal & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Batal",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(selectedPlatforms) },
                        enabled = selectedPlatforms.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            disabledContainerColor = Color(0x331E293B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPlatforms.isNotEmpty()) Color.White else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun MicrostockSelectionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(115.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0x331E293B).let { accentColor.copy(alpha = 0.12f) } else Color(0x440F172A)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) accentColor else Color(0x33334155)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) accentColor else Color.Transparent,
                    border = BorderStroke(1.5.dp, if (isSelected) accentColor else Color(0xFF64748B)),
                    modifier = Modifier.size(18.dp)
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
