package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullScreenCodeEditorModal
import com.example.ui.screens.EngineScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassHeaderBackground
import com.example.ui.theme.GlassHeaderBorder
import com.example.ui.theme.GlassNavBackground
import com.example.ui.theme.GlassNavBorder
import com.example.ui.theme.MainBackgroundGradient
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SkyGlow
import com.example.viewmodel.CodeMotionViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private val viewModel: CodeMotionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CodeMotionApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CodeMotionApp(
    viewModel: CodeMotionViewModel
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val userCode by viewModel.userCode.collectAsState()
    var isFullscreenEditorOpen by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundGradient)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Glassmorphism Header Bar (Sky blue to purple gradient with glass effect)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, GlassHeaderBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GlassHeaderBackground)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0x3338BDF8),
                                        border = BorderStroke(1.dp, Color(0x5538BDF8)),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Code,
                                                contentDescription = null,
                                                tint = SkyGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "WAR ",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Text(
                                            text = "MOTION",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFF6D00),
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewModel.setActiveTab(NavigationTab.ENGINE) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (activeTab == NavigationTab.ENGINE) Color(0x66FF6D00) else Color(0x3338BDF8),
                                    border = BorderStroke(1.dp, if (activeTab == NavigationTab.ENGINE) Color(0xFFFF9E40) else Color(0x6638BDF8))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9E40),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "ENGINE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 1.sp,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Glassmorphism Bottom Navigation Bar (Gradient with aesthetic frosted glass)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, GlassNavBorder)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            modifier = Modifier
                                .background(GlassNavBackground)
                                .clip(RoundedCornerShape(22.dp)),
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == NavigationTab.STUDIO,
                                onClick = { viewModel.setActiveTab(NavigationTab.STUDIO) },
                                icon = {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = "Studio")
                                },
                                label = { Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = SkyGlow,
                                    indicatorColor = Color(0x660284C7),
                                    unselectedIconColor = Color(0xFF818CF8).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF818CF8).copy(alpha = 0.6f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == NavigationTab.EXPORT,
                                onClick = { viewModel.setActiveTab(NavigationTab.EXPORT) },
                                icon = {
                                    Icon(imageVector = Icons.Default.MovieCreation, contentDescription = "Export")
                                },
                                label = { Text("Export", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = SkyGlow,
                                    indicatorColor = Color(0x660284C7),
                                    unselectedIconColor = Color(0xFF818CF8).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF818CF8).copy(alpha = 0.6f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == NavigationTab.GALLERY,
                                onClick = { viewModel.setActiveTab(NavigationTab.GALLERY) },
                                icon = {
                                    Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = "Gallery")
                                },
                                label = { Text("Galeri", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = SkyGlow,
                                    indicatorColor = Color(0x660284C7),
                                    unselectedIconColor = Color(0xFF818CF8).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF818CF8).copy(alpha = 0.6f)
                                )
                            )

                            NavigationBarItem(
                                selected = activeTab == NavigationTab.ENGINE,
                                onClick = { viewModel.setActiveTab(NavigationTab.ENGINE) },
                                icon = {
                                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Engine")
                                },
                                label = { Text("Engine", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFFF9E40),
                                    indicatorColor = Color(0x66FF6D00),
                                    unselectedIconColor = Color(0xFF818CF8).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF818CF8).copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    NavigationTab.STUDIO -> {
                        StudioScreen(
                            viewModel = viewModel,
                            onWebViewReady = { wv -> viewModel.bindWebView(wv) },
                            onOpenFullscreen = { isFullscreenEditorOpen = true }
                        )
                    }
                    NavigationTab.EXPORT -> {
                        ExportScreen(
                            viewModel = viewModel
                        )
                    }
                    NavigationTab.GALLERY -> {
                        GalleryScreen(
                            viewModel = viewModel
                        )
                    }
                    NavigationTab.ENGINE -> {
                        EngineScreen(
                            viewModel = viewModel
                        )
                    }
                }

                // Mode Terminal Fullscreen Termux
                if (isFullscreenEditorOpen) {
                    FullScreenCodeEditorModal(
                        code = userCode,
                        onCodeChange = { viewModel.updateCode(it) },
                        onDismiss = {
                            isFullscreenEditorOpen = false
                            viewModel.applyCodeToPreview()
                        }
                    )
                }
            }
        }
    }
}
