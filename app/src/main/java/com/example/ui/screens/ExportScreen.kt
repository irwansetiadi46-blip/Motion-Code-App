package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MicrostockPlatform
import com.example.model.OutputMode
import com.example.model.QualityBitrate
import com.example.model.RenderState
import com.example.ui.components.ExportConfigurationCard
import com.example.ui.components.LiveAnimationViewport
import com.example.ui.components.MicrostockExportDialog
import com.example.ui.components.OutputModeSelectorCard
import com.example.ui.components.RenderedImageView
import com.example.ui.components.StockMetadataCard
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import com.example.viewmodel.CodeMotionViewModel
import com.example.viewmodel.DownloadState
import java.text.DecimalFormat

@Composable
fun ExportScreen(
    viewModel: CodeMotionViewModel,
    modifier: Modifier = Modifier
) {
    val userCode by viewModel.userCode.collectAsState()
    val renderConfig by viewModel.renderConfig.collectAsState()
    val renderState by viewModel.renderState.collectAsState()
    val selectedVideo by viewModel.selectedVideoForPlayback.collectAsState()
    val lastRenderedImage by viewModel.lastRenderedImage.collectAsState()
    val outputMode by viewModel.outputMode.collectAsState()
    val selectedImageFormat by viewModel.selectedImageFormat.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val reloadTrigger by viewModel.reloadPreviewTrigger.collectAsState()
    val currentMetadata by viewModel.currentMetadata.collectAsState()
    val isGeneratingMetadata by viewModel.isGeneratingMetadata.collectAsState()
    val failoverDialogState by viewModel.failoverDialogState.collectAsState()

    var showMicrostockDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val isRendering = renderState is RenderState.Rendering || renderState is RenderState.Preparing || renderState is RenderState.Finalizing
        val context = androidx.compose.ui.platform.LocalContext.current
        val engineWebView = remember { viewModel.getOrCreateEngineWebView(context) }

        // Mode Selector: Videos / Images (Synchronized across Studio & Export tabs)
        OutputModeSelectorCard(
            currentMode = outputMode,
            onModeSelected = { viewModel.setOutputMode(it) }
        )

        // Section: Live Viewport Preview
        LiveAnimationViewport(
            userCode = userCode,
            renderConfig = renderConfig,
            bridge = viewModel.bridge,
            reloadTrigger = reloadTrigger,
            onWebViewReady = { wv ->
                viewModel.bindWebView(wv)
            },
            onWebViewDisposed = {
                // Keep engine webview alive during tab switch
            },
            title = if (outputMode == OutputMode.IMAGES) "VIEWPORT GAMBAR" else "REAL-TIME ANIMATION VIEWPORT",
            isRendering = isRendering,
            engineWebView = engineWebView
        )

        // Tombol Render Mp4 / Render Images di tab export persis di bawah Viewport di atas Container Configuration
        Button(
            onClick = {
                if (isRendering) {
                    viewModel.cancelRender()
                } else {
                    viewModel.startRenderVideo()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRendering) Color(0xFFE11D48) else ElectricBlue
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isRendering) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Render",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(
                    imageVector = if (outputMode == OutputMode.VIDEOS) Icons.Default.Movie else Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (outputMode == OutputMode.VIDEOS) "Render Mp4" else "Render Images",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Live Render Progress Section
        AnimatedVisibility(visible = isRendering) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, ElectricCyan)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val progress = when (val s = renderState) {
                        is RenderState.Rendering -> s.progress
                        is RenderState.Finalizing -> 0.98f
                        else -> 0f
                    }
                    val statusText = when (val s = renderState) {
                        is RenderState.Preparing -> s.message
                        is RenderState.Rendering -> s.statusText
                        is RenderState.Finalizing -> s.message
                        else -> "Menyiapkan..."
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SkyGlow,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Container Export Configuration yang menyesuaikan secara cerdas antara Videos dan Images
        ExportConfigurationCard(
            renderConfig = renderConfig,
            onResolutionChange = { viewModel.updateResolution(it) },
            onDurationChange = { viewModel.updateDuration(it) },
            onFpsChange = { viewModel.updateFps(it) },
            onQualityChange = { viewModel.updateQuality(it) },
            outputMode = outputMode,
            selectedImageFormat = selectedImageFormat,
            onImageFormatChange = { viewModel.setImageFormat(it) }
        )

        // Error message if any
        if (renderState is RenderState.Error) {
            val errMsg = (renderState as RenderState.Error).message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1824)),
                border = BorderStroke(1.dp, Color(0xFFF43F5E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = errMsg,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFECDD3))
                    )
                }
            }
        }

        // Section: Media Preview (Displayed after render finishes)
        if (outputMode == OutputMode.IMAGES && lastRenderedImage != null) {
            RenderedImageView(
                image = lastRenderedImage!!,
                selectedFormat = selectedImageFormat,
                onFormatChange = { viewModel.setImageFormat(it) },
                onExport = { viewModel.downloadImage(lastRenderedImage!!) },
                onDelete = { viewModel.deleteImage(lastRenderedImage!!) }
            )
        } else if (outputMode == OutputMode.VIDEOS && selectedVideo != null) {
            VideoPlayerView(
                video = selectedVideo!!,
                onDownload = { viewModel.downloadVideo(selectedVideo!!) },
                onDelete = { viewModel.deleteVideo(selectedVideo!!) }
            )
        }

        // Section: Stock Footage Metadata (File Name, Title, Description, Keywords)
        StockMetadataCard(
            metadata = currentMetadata,
            onMetadataChange = { viewModel.updateCurrentMetadata(it) },
            onDownloadCsv = {
                showMicrostockDialog = true
            },
            onGenerateMetadata = {
                viewModel.generateMetadataFromEditor()
            },
            onCancelGenerate = {
                viewModel.cancelMetadataGeneration()
            },
            isGenerating = isGeneratingMetadata,
            videoFilename = selectedVideo?.file?.name ?: "Vid-WM.mp4"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Popup: Pilihan Format Microstock (Adobe Stock, Shutterstock, Vecteezy) format Card 2 kolom
    if (showMicrostockDialog) {
        MicrostockExportDialog(
            onDismiss = { showMicrostockDialog = false },
            onSave = { platforms ->
                val baseName = currentMetadata.filename.ifBlank {
                    selectedVideo?.file?.name?.removeSuffix(".mp4") ?: "animation"
                }
                viewModel.downloadMicrostockCsv(listOf(currentMetadata), platforms, baseName)
                showMicrostockDialog = false
            }
        )
    }

    // Modal Dialog: Indikator Progres Download & Spinner dengan Persentase
    when (val dState = downloadState) {
        is DownloadState.Downloading -> {
            Dialog(
                onDismissRequest = { /* Prevent dismiss while downloading */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, SkyGlow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SkyGlow,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = if (outputMode == OutputMode.IMAGES) "Mengekspor Gambar..." else "Mengunduh Video MP4...",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "${dState.percent}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricCyan
                            )
                        )

                        LinearProgressIndicator(
                            progress = { dState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SkyGlow,
                            trackColor = Color(0xFF1E293B)
                        )

                        Text(
                            text = "Menyimpan ke memori perangkat:\nFolder Download/Code Motion Video",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        }

        is DownloadState.Success -> {
            Dialog(onDismissRequest = { viewModel.dismissDownloadState() }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, MatrixGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(52.dp)
                        )

                        Text(
                            text = "Download Selesai 100%!",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = if (outputMode == OutputMode.IMAGES)
                                "File gambar berhasil disimpan ke memori lokal di:\n\n${dState.folderName}/${dState.filename}"
                            else
                                "File video MP4 berhasil disimpan ke memori lokal di:\n\n${dState.folderName}/${dState.filename}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        )

                        Button(
                            onClick = { viewModel.dismissDownloadState() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Selesai", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        is DownloadState.Error -> {
            Dialog(onDismissRequest = { viewModel.dismissDownloadState() }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Download Gagal",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = dState.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFECDD3),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )

                        Button(
                            onClick = { viewModel.dismissDownloadState() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tutup")
                        }
                    }
                }
            }
        }

        else -> {}
    }

    // Modal Popup: Auto Failover Dialog (jika terjadi error API atau melewati batas waktu 30 detik)
    if (failoverDialogState.isOpen) {
        Dialog(
            onDismissRequest = { viewModel.dismissFailoverDialog() },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33F59E0B),
                        border = BorderStroke(1.dp, Color(0x66F59E0B)),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = if (failoverDialogState.isTimeout) "Waktu Generate Habis (30s)" else "Kendala Auto Failover",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = failoverDialogState.reason,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFDE68A),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier
                            .background(Color(0x33000000), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .fillMaxWidth()
                    )

                    Text(
                        text = "Terjadi kendala pada proses generate metadata. Ingin melanjutkan generate ulang (Auto Failover) atau batalkan?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tombol Batal: Jika Batal maka tidak Generate ulang
                        OutlinedButton(
                            onClick = { viewModel.dismissFailoverDialog() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF94A3B8)
                            )
                        ) {
                            Text(
                                text = "Batal",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Tombol Lanjutkan: Jika Lanjut maka generate ulang
                        Button(
                            onClick = { viewModel.continueFailoverAndRetry() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Lanjutkan",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
