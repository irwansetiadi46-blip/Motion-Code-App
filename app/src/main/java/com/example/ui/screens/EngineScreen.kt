package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import com.example.util.CsvExporter
import com.example.viewmodel.CodeMotionViewModel

@Composable
fun EngineScreen(
    viewModel: CodeMotionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiKeys by viewModel.userApiKeys.collectAsState()
    val activeIndex by viewModel.activeApiKeyIndex.collectAsState()

    var inputKey by remember { mutableStateOf("") }
    var revealedIndices by remember { mutableStateOf(setOf<Int>()) }

    val glassBorderBrush = Brush.horizontalGradient(
        listOf(
            Color(0x5538BDF8),
            Color(0x66A855F7),
            Color(0x4438BDF8)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card (Glassmorphism)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x990C132E)),
            border = BorderStroke(1.2.dp, glassBorderBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33FF6D00),
                    border = BorderStroke(1.dp, Color(0x66FF6D00)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = Color(0xFFFF9E40),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Engine & API Key",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kelola API key Google Gemini untuk generate metadata otomatis dengan dukungan auto failover.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Input Card with flexible textarea up to 3 lines and Add button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, PurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah API Key Baru",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                // Flexible textarea (expands up to 3 lines) with Add button inside
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = {
                        Text(
                            text = "Tempel Google Gemini API Key di sini...",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x66080E24),
                        unfocusedContainerColor = Color(0x44080E24),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFFFF6D00),
                        unfocusedBorderColor = DarkBorder.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        Button(
                            onClick = {
                                val trimmed = inputKey.trim()
                                if (trimmed.isNotEmpty()) {
                                    val success = viewModel.addApiKey(trimmed)
                                    if (success) {
                                        inputKey = ""
                                        Toast.makeText(context, "API Key berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "API Key sudah terdaftar atau tidak valid", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Silakan masukkan API key", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Add",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                )

                Text(
                    text = "Daftarkan satu atau lebih API key. Jika salah satu kuota limit atau error, sistem akan otomatis berpindah (failover) ke API key cadangan.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                )
            }
        }

        // List API Key Card with Radio Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, PurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar API Key (${apiKeys.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    if (apiKeys.isNotEmpty()) {
                        Text(
                            text = "Pilih key aktif via radio",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                if (apiKeys.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x331E293B),
                        border = BorderStroke(1.dp, Color(0x22334155))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Belum Ada API Key Ditambahkan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0)
                                )
                            )
                            Text(
                                text = "Masukkan API key Gemini Anda pada kotak input di atas lalu klik Add.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apiKeys.forEachIndexed { index, key ->
                            val isSelected = (index == activeIndex)
                            val isRevealed = revealedIndices.contains(index)
                            val displayKey = if (isRevealed) {
                                key
                            } else {
                                maskApiKey(key)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setActiveApiKeyIndex(index) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0x550C2A44) else Color(0x33080E24),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF6D00) else DarkBorder.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.setActiveApiKeyIndex(index) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFFFF6D00),
                                                unselectedColor = Color(0xFF64748B)
                                            )
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Key #${index + 1}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color(0xFFFFB74D) else Color.White
                                                    )
                                                )
                                                if (isSelected) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0x33FF6D00),
                                                        border = BorderStroke(0.8.dp, Color(0xFFFF6D00))
                                                    ) {
                                                        Text(
                                                            text = "AKTIF",
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFFB74D)
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = displayKey,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        // Toggle visibility
                                        IconButton(
                                            onClick = {
                                                revealedIndices = if (isRevealed) {
                                                    revealedIndices - index
                                                } else {
                                                    revealedIndices + index
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Lihat Key",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Copy key
                                        IconButton(
                                            onClick = {
                                                CsvExporter.copyToClipboard(context, "API Key #${index + 1}", key)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Salin Key",
                                                tint = SkyGlow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete key
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteApiKey(index)
                                                Toast.makeText(context, "API Key #${index + 1} dihapus", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Key",
                                                tint = Color(0xFFF43F5E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Informative Auto Failover Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x44081024)),
            border = BorderStroke(1.dp, Color(0x3338BDF8))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MatrixGreen,
                    modifier = Modifier.size(22.dp)
                )

                Column {
                    Text(
                        text = "Auto-Failover Round-Robin",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Ketika generate metadata, sistem mencoba key yang dipilih. Jika kuota habis atau terjadi eror, engine langsung berpindah ke key berikutnya secara otomatis dan berputar sampai sukses.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun maskApiKey(key: String): String {
    return if (key.length <= 10) {
        "••••••••••••"
    } else {
        "${key.take(6)}••••••••${key.takeLast(4)}"
    }
}
