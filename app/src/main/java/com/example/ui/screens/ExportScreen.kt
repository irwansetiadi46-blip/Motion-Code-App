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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.model.QualityBitrate
import com.example.model.RenderState
import com.example.ui.components.LiveAnimationViewport
import com.example.ui.components.ResolutionSelector
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
    val downloadState by viewModel.downloadState.collectAsState()
    val reloadTrigger by viewModel.reloadPreviewTrigger.collectAsState()
    val currentMetadata by viewModel.currentMetadata.collectAsState()
    val isGeneratingMetadata by viewModel.isGeneratingMetadata.collectAsState()

    val fpsOptions = listOf(24, 30, 60)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Live Viewport Preview
        Column {
            Text(
                text = "Live Viewport Preview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Resolusi aktif: ${renderConfig.resolution.width}×${renderConfig.resolution.height}. Animasi berjalan secara real-time dari kode yang Anda tulis.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(8.dp))

            LiveAnimationViewport(
                userCode = userCode,
                renderConfig = renderConfig,
                bridge = viewModel.bridge,
                reloadTrigger = reloadTrigger,
                onWebViewReady = { wv ->
                    viewModel.bindWebView(wv)
                },
                onWebViewDisposed = {
                    viewModel.bindWebView(null)
                },
                title = "REAL-TIME ANIMATION VIEWPORT"
            )
        }

        // Section: Konfigurasi Export
        Text(
            text = "Pengaturan Format & Export Video",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // Resolution & Aspect Ratio Selector (including 4K UHD)
        ResolutionSelector(
            selectedResolution = renderConfig.resolution,
            onResolutionSelected = { viewModel.updateResolution(it) }
        )

        // Duration & FPS Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, PurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Duration Slider
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
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SkyGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Durasi Video",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${renderConfig.durationSeconds} Detik",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        )
                    }
                }

                Slider(
                    value = renderConfig.durationSeconds.toFloat(),
                    onValueChange = { viewModel.updateDuration(it.toInt()) },
                    valueRange = 3f..30f,
                    steps = 26,
                    colors = SliderDefaults.colors(
                        thumbColor = SkyGlow,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // FPS Selector
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
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = SkyGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Frame Rate (FPS)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        fpsOptions.forEach { fps ->
                            val isSelected = renderConfig.fps == fps
                            Surface(
                                onClick = { viewModel.updateFps(fps) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ElectricBlue else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSelected) SkyGlow else DarkBorder)
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bitrate / Quality Options
                Text(
                    text = "Bitrate / Kualitas Output:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityBitrate.ALL.forEach { q ->
                        val isSelected = renderConfig.quality.bitrateBps == q.bitrateBps
                        Surface(
                            onClick = { viewModel.updateQuality(q) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Color(0xFF0C2A44) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isSelected) SkyGlow else DarkBorder)
                        ) {
                            Text(
                                text = q.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) SkyGlow else Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Render / Cancel Action Button
        val isRendering = renderState is RenderState.Rendering || renderState is RenderState.Preparing || renderState is RenderState.Finalizing
        Button(
            onClick = {
                if (isRendering) {
                    viewModel.cancelRender()
                } else {
                    viewModel.triggerRender()
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
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Render Mp4", fontWeight = FontWeight.Bold, color = Color.White)
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

        // Section: Video MP4 Preview (Displayed after render finishes)
        if (selectedVideo != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Video Mp4 Preview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                VideoPlayerView(
                    video = selectedVideo!!,
                    onDownload = { viewModel.downloadVideo(selectedVideo!!) },
                    onShare = { viewModel.shareVideo(selectedVideo!!) },
                    onDelete = { viewModel.deleteVideo(selectedVideo!!) }
                )
            }
        }

        // Section: Stock Footage Metadata (Title, Description, Keywords)
        StockMetadataCard(
            metadata = currentMetadata,
            onMetadataChange = { viewModel.updateCurrentMetadata(it) },
            onGenerateAi = { viewModel.generateMetadataForCurrentAnimation() },
            onDownloadCsv = {
                val videoFilename = selectedVideo?.file?.name ?: "code_motion_video.mp4"
                viewModel.downloadMetadataCsv(videoFilename, currentMetadata)
            },
            onShareCsv = {
                val videoFilename = selectedVideo?.file?.name ?: "code_motion_video.mp4"
                viewModel.shareMetadataCsv(videoFilename, currentMetadata)
            },
            isGenerating = isGeneratingMetadata,
            videoFilename = selectedVideo?.file?.name ?: "code_motion_video.mp4"
        )

        Spacer(modifier = Modifier.height(24.dp))
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
                            text = "Mengunduh Video MP4...",
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
                            text = "File video MP4 berhasil disimpan ke memori lokal di:\n\n${dState.folderName}/${dState.filename}",
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
}
