package com.example.ui.screens

import android.webkit.WebView
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
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetRepository
import com.example.model.VideoPreset
import com.example.ui.components.CodeTerminalEditor
import com.example.ui.components.LiveAnimationViewport
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SkyGlow
import com.example.viewmodel.CodeMotionViewModel
import com.example.viewmodel.NavigationTab

@Composable
fun StudioScreen(
    viewModel: CodeMotionViewModel,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val userCode by viewModel.userCode.collectAsState()
    val renderConfig by viewModel.renderConfig.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val reloadTrigger by viewModel.reloadPreviewTrigger.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Quick Preset Chips
        Column {
            Text(
                text = "Preset Animasi:",
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
                        border = androidx.compose.foundation.BorderStroke(
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

        // Live Animation Stage (Real-time Viewport)
        LiveAnimationViewport(
            userCode = userCode,
            renderConfig = renderConfig,
            bridge = viewModel.bridge,
            reloadTrigger = reloadTrigger,
            onWebViewReady = onWebViewReady
        )

        // Navigation CTA to Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.setActiveTab(NavigationTab.EXPORT) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfigurasi & Render Video",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Code Editor
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
                    text = "Real-time sync",
                    style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            CodeTerminalEditor(
                code = userCode,
                onCodeChange = { viewModel.updateCode(it) },
                onApplyCode = { viewModel.applyCodeToPreview() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
