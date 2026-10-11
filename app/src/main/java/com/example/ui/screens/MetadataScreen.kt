package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MicrostockPlatform
import com.example.model.VideoMetadata
import com.example.ui.components.MicrostockExportDialog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SkyGlow
import com.example.util.CsvExporter
import com.example.util.MetadataValidator
import com.example.viewmodel.CodeMotionViewModel

@Composable
fun MetadataScreen(
    viewModel: CodeMotionViewModel,
    modifier: Modifier = Modifier
) {
    val metadataList by viewModel.savedMetadataList.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<VideoMetadata?>(null) }
    var showExportAllDialog by remember { mutableStateOf(false) }
    var singleItemToExport by remember { mutableStateOf<VideoMetadata?>(null) }

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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Container Card: Header Bar dengan Icon Download Csv di kanan & Tombol Hapus Semua
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x990C132E)),
            border = BorderStroke(1.2.dp, glassBorderBrush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Metadata List & Item Count
                Row(
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
                                imageVector = Icons.Default.DataObject,
                                contentDescription = null,
                                tint = SkyGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Metadata",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${metadataList.size} item tersimpan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Tombol-tombol di sebelah kanan: Download Csv & Hapus Semua
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol Icon Download dengan teks "Csv" untuk download semua metadata
                    Button(
                        onClick = { showExportAllDialog = true },
                        enabled = metadataList.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            disabledContainerColor = Color(0x331E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download All CSV",
                            modifier = Modifier.size(15.dp),
                            tint = if (metadataList.isNotEmpty()) Color.White else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Csv",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (metadataList.isNotEmpty()) Color.White else Color(0xFF64748B)
                            )
                        )
                    }

                    // Tombol Hapus Semua untuk hapus permanen semua list beserta metadata
                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        enabled = metadataList.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF43F5E),
                            disabledContentColor = Color(0xFF64748B)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (metadataList.isNotEmpty()) Color(0x66F43F5E) else Color(0x2264748B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Semua",
                            modifier = Modifier.size(15.dp),
                            tint = if (metadataList.isNotEmpty()) Color(0xFFF43F5E) else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hapus Semua",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (metadataList.isNotEmpty()) Color(0xFFF43F5E) else Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }

        // List Metadata atau Empty State
        if (metadataList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x770C132E)),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x2238BDF8),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = SkyGlow.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "Belum Ada Metadata Tersimpan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Ketika Anda menekan Render Mp4 di tab Export, proses render dan generate metadata berjalan paralel. Metadata otomatis tersimpan dan ditampilkan di sini.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = metadataList, key = { it.id }) { item ->
                    MetadataRowAccordionItem(
                        item = item,
                        onUpdate = { updated -> viewModel.updateSavedMetadata(updated) },
                        onDelete = { itemToDelete = item },
                        onDownloadCsv = { singleItemToExport = item }
                    )
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus Semua
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = "Hapus Semua Metadata?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Semua ${metadataList.size} riwayat metadata yang tersimpan akan dihapus secara permanen. Tindakan ini tidak dapat dibatalkan.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllSavedMetadata()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                ) {
                    Text("Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Dialog Konfirmasi Hapus Individu Item
    if (itemToDelete != null) {
        val target = itemToDelete!!
        val displayName = target.filename.ifBlank { target.title.ifBlank { "Item ini" } }
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = "Hapus Metadata?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Metadata \"$displayName\" akan dihapus secara permanen dari daftar.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavedMetadata(target.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Modal Popup: Ekspor Semua Metadata ke Format Microstock (Adobe Stock, Shutterstock, Vecteezy)
    if (showExportAllDialog) {
        MicrostockExportDialog(
            onDismiss = { showExportAllDialog = false },
            onSave = { platforms ->
                viewModel.downloadMicrostockCsv(metadataList, platforms, "all_stock_metadata")
                showExportAllDialog = false
            }
        )
    }

    // Modal Popup: Ekspor Item Tunggal ke Format Microstock
    if (singleItemToExport != null) {
        val targetItem = singleItemToExport!!
        MicrostockExportDialog(
            onDismiss = { singleItemToExport = null },
            onSave = { platforms ->
                val baseName = targetItem.filename.ifBlank { "stock_metadata" }
                viewModel.downloadMicrostockCsv(listOf(targetItem), platforms, baseName)
                singleItemToExport = null
            }
        )
    }
}

/**
 * Tampilan List Row satu baris dengan tampilan Filename-nya saja.
 * Bisa diklik untuk menampilkan isinya dengan tampilan metadata seperti di tab export dengan metode dropdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetadataRowAccordionItem(
    item: VideoMetadata,
    onUpdate: (VideoMetadata) -> Unit,
    onDelete: () -> Unit,
    onDownloadCsv: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var inputKeyword by remember { mutableStateOf("") }
    val validation = remember(item) { MetadataValidator.validate(item) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    val displayName = if (item.filename.isNotBlank()) item.filename else item.title.ifBlank { "Untitled Animation" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x880E1838)),
        border = BorderStroke(
            1.dp,
            if (isExpanded) SkyGlow.copy(alpha = 0.8f) else Color(0x3338BDF8)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row satu baris dengan tampilan Filename-nya saja
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x2238BDF8),
                        border = BorderStroke(1.dp, Color(0x4438BDF8)),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = SkyGlow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tombol Hapus Di tiap list metadata
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x22F43F5E),
                        border = BorderStroke(1.dp, Color(0x44F43F5E)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Metadata",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Icon Chevron Dropdown
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Tutup" else "Buka Dropdown",
                            tint = SkyGlow,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }

            // Dropdown Accordion Content: Tampilan Metadata Seperti di Tab Export
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x55060C20))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0x2238BDF8), thickness = 1.dp)

                    // Kolom File Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "File Name",
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
                                    text = "${validation.filenameWordCount}/5 words",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (validation.isFilenameValid && item.filename.isNotBlank()) MatrixGreen else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        CsvExporter.copyToClipboard(context, "File Name", item.filename)
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin File Name",
                                        tint = SkyGlow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = item.filename,
                            onValueChange = { onUpdate(item.copy(filename = it)) },
                            placeholder = {
                                Text("e.g. Neon Wave Animation Loop", color = Color(0xFF64748B), fontSize = 13.sp)
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
                            singleLine = true
                        )
                    }

                    // Kolom Title (English)
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
                                        CsvExporter.copyToClipboard(context, "Title", item.title)
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
                            value = item.title,
                            onValueChange = { onUpdate(item.copy(title = it)) },
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
                            maxLines = 2
                        )
                    }

                    // Kolom Description (English)
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
                                        CsvExporter.copyToClipboard(context, "Description", item.description)
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
                            value = item.description,
                            onValueChange = { onUpdate(item.copy(description = it)) },
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

                    // Kolom Keywords
                    val currentKeywordsList = remember(item.keywords) { item.parseKeywordsList() }
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
                                    text = "${validation.keywordCount}/49",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (validation.isKeywordsValid) MatrixGreen else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        CsvExporter.copyToClipboard(context, "Keywords", item.cleanKeywordsString())
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
                                Surface(
                                    onClick = { onUpdate(item.copy(keywords = "")) },
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

                        // Kolom Input keyword dengan tombol Add
                        OutlinedTextField(
                            value = inputKeyword,
                            onValueChange = { inputKeyword = it },
                            placeholder = {
                                Text("Ketik keyword...", color = Color(0xFF64748B), fontSize = 13.sp)
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
                                            val newTags = trimmed.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                            val combined = (currentKeywordsList + newTags).distinct()
                                            onUpdate(item.copy(keywords = combined.joinToString(", ")))
                                            inputKeyword = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Add", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        )

                        // FlowRow Keywords Chips
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
                                                            onUpdate(item.copy(keywords = updatedList.joinToString(", ")))
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tombol Aksi: Hapus Item & Download CSV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(0.38f)
                                .height(42.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
                            border = BorderStroke(1.dp, Color(0x66F43F5E)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFF43F5E)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hapus",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = onDownloadCsv,
                            modifier = Modifier
                                .weight(0.62f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp)
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
                    }
                }
            }
        }
    }
}
