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
import com.example.model.QualityBitrate
import com.example.model.RenderState
import com.example.ui.components.LiveAnimationViewport
import com.example.ui.components.ResolutionSelector
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SkyGlow
import com.example.viewmodel.CodeMotionViewModel
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
    val reloadTrigger by viewModel.reloadPreviewTrigger.collectAsState()

    val fpsOptions = listOf(24, 30, 60)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 2: Preview Render (Active Live Stage)
        Column {
            Text(
                text = "2. Preview Render & Animasi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Viewport preview = resolusi output (${renderConfig.resolution.width}×${renderConfig.resolution.height}). Pratinjau frame yang sedang di-render akan tampil di sini secara real-time.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(8.dp))

            LiveAnimationViewport(
                userCode = userCode,
                renderConfig = renderConfig,
                bridge = viewModel.bridge,
                reloadTrigger = reloadTrigger,
                onWebViewReady = { wv ->
                    viewModel.boundWebView = wv
                },
                title = "PREVIEW RENDER (LIVE)"
            )
        }

        // Section 3: Konfigurasi Export
        Text(
            text = "3. Konfigurasi Export",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // Resolution & Aspect Ratio Selector
        ResolutionSelector(
            selectedResolution = renderConfig.resolution,
            onResolutionSelected = { viewModel.updateResolution(it) }
        )

        // Duration & FPS Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
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

        // Summary Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1426)),
            border = BorderStroke(1.dp, Color(0xFF1E3A5F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SkyGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Ringkasan Render Deterministik",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                val estMb = DecimalFormat("#.##").format(renderConfig.estimatedSizeBytes / (1024f * 1024f))
                Text(
                    text = "• Total Frame: ${renderConfig.totalFrames} frame (${renderConfig.durationSeconds}s × ${renderConfig.fps}fps)\n" +
                            "• Bitrate Efektif: ${(renderConfig.safeBitrateBps / 1_000_000f)} Mbps (Hardware AVC/H.264)\n" +
                            "• Estimasi Ukuran File: ~$estMb MB MP4\n" +
                            "• Model Render: Deterministic Virtual Clock (0 frame drop, pixel-perfect)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Render Action Button
        val isRendering = renderState is RenderState.Rendering || renderState is RenderState.Preparing || renderState is RenderState.Finalizing
        Button(
            onClick = { viewModel.triggerRender() },
            enabled = !isRendering,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isRendering) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sedang Merender...", fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("🎬 Render ke MP4 (${renderConfig.resolution.width}×${renderConfig.resolution.height})", fontWeight = FontWeight.Bold)
            }
        }

        // Progress Section
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

        // Section 4: Output Video
        if (selectedVideo != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "4. Output Video",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                VideoPlayerView(
                    video = selectedVideo!!,
                    onDownload = { viewModel.downloadVideo(selectedVideo!!) },
                    onShare = { viewModel.shareVideo(selectedVideo!!) },
                    onDelete = { viewModel.deleteVideo(selectedVideo!!) },
                    onSaveToGallery = { viewModel.saveVideoToGallery(selectedVideo!!) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
