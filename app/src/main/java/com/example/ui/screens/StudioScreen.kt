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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.PresetRepository
import com.example.model.RenderState
import com.example.ui.components.CodeTerminalEditor
import com.example.ui.components.LiveAnimationViewport
import com.example.ui.components.ResolutionSelector
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SkyGlow
import com.example.viewmodel.CodeMotionViewModel
import com.example.viewmodel.DownloadState

@Composable
fun StudioScreen(
    viewModel: CodeMotionViewModel,
    onWebViewReady: (WebView) -> Unit,
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userCode by viewModel.userCode.collectAsState()
    val renderConfig by viewModel.renderConfig.collectAsState()
    val renderState by viewModel.renderState.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val reloadTrigger by viewModel.reloadPreviewTrigger.collectAsState()
    val lastRenderedVideo by viewModel.lastRenderedVideo.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()

    var showExportSettings by remember { mutableStateOf(false) }

    val isRendering = renderState is RenderState.Rendering || renderState is RenderState.Preparing || renderState is RenderState.Finalizing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Presets Selector
        Column {
            Text(
                text = "Preset Animasi (Template Opsional):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetRepository.PRESETS.forEach { preset ->
                    val isSelected = preset.id == selectedPreset.id
                    Surface(
                        onClick = { viewModel.selectPreset(preset) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) SkyGlow else DarkBorder
                        )
                    ) {
                        Text(
                            text = preset.name,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }
        }

        // Live Animation Stage (Real-time Viewport from user's code)
        LiveAnimationViewport(
            userCode = userCode,
            renderConfig = renderConfig,
            bridge = viewModel.bridge,
            reloadTrigger = reloadTrigger,
            onWebViewReady = onWebViewReady,
            onWebViewDisposed = {
                viewModel.bindWebView(null)
            },
            title = "VIEWPORT ANIMASI REAL-TIME"
        )

        // Render Action & Export Settings Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = SkyGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Render ke Video MP4",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Surface(
                        onClick = { showExportSettings = !showExportSettings },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${renderConfig.resolution.width}×${renderConfig.resolution.height} • ${renderConfig.fps}fps",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = ElectricCyan
                                )
                            )
                            Icon(
                                imageVector = if (showExportSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Expandable Settings: Resolution, FPS, Duration
                AnimatedVisibility(visible = showExportSettings) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih Resolusi & Rasio Aspek (Hingga Full HD 1080p):",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                        ResolutionSelector(
                            selectedResolution = renderConfig.resolution,
                            onResolutionSelected = { viewModel.updateResolution(it) }
                        )

                        // Duration Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Durasi Video:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                            Text(
                                text = "${renderConfig.durationSeconds} Detik",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            )
                        }
                        Slider(
                            value = renderConfig.durationSeconds.toFloat(),
                            onValueChange = { viewModel.updateDuration(it.toInt()) },
                            valueRange = 3f..20f,
                            steps = 16,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )

                        // FPS selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Frame Rate:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                            listOf(24, 30, 60).forEach { f ->
                                val isF = renderConfig.fps == f
                                Surface(
                                    onClick = { viewModel.updateFps(f) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isF) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isF) SkyGlow else DarkBorder)
                                ) {
                                    Text(
                                        text = "${f}fps",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isF) Color.White else Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Render Action Button
                Button(
                    onClick = { viewModel.triggerRender() },
                    enabled = !isRendering,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isRendering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang Merender...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Render to MP4 (${renderConfig.resolution.width}×${renderConfig.resolution.height})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Render Progress Indicator Bar
                AnimatedVisibility(visible = isRendering) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
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
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SkyGlow,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }

                // Error Message Card if render fails
                if (renderState is RenderState.Error) {
                    val errMsg = (renderState as RenderState.Error).message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1824)),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errMsg,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFECDD3))
                            )
                        }
                    }
                }
            }
        }

        // Section: Video Mp4 Preview (Displayed after render finishes or when video selected)
        if (lastRenderedVideo != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Video Mp4 Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Hasil Render Siap Download",
                        style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen)
                    )
                }

                VideoPlayerView(
                    video = lastRenderedVideo!!,
                    onDownload = { viewModel.downloadVideo(lastRenderedVideo!!) },
                    onShare = { viewModel.shareVideo(lastRenderedVideo!!) },
                    onDelete = { viewModel.deleteVideo(lastRenderedVideo!!) },
                    onSaveToGallery = { viewModel.saveVideoToGallery(lastRenderedVideo!!) }
                )
            }
        }

        // Code Editor with Fullscreen Trigger & Syntax Highlighting
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kode Animasi (HTML5 Canvas / JS)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "● Real-Time Aktif",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MatrixGreen
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            CodeTerminalEditor(
                code = userCode,
                onCodeChange = { viewModel.updateCode(it) },
                onApplyCode = { viewModel.applyCodeToPreview() },
                onOpenFullscreen = onOpenFullscreen
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Modal Dialog: Indikator Progres Download & Spinner dengan Persentase ke Folder Download/Code Motion Video
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
                            text = "Menyimpan ke memori lokal perangkat:\nFolder Download/Code Motion Video",
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
