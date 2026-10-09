package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.EngineHtmlBuilder
import com.example.engine.WebCodecsBridge
import com.example.model.RenderConfig
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.SkyGlow
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveAnimationViewport(
    userCode: String,
    renderConfig: RenderConfig,
    bridge: WebCodecsBridge,
    reloadTrigger: Long,
    onWebViewReady: (WebView) -> Unit,
    onWebViewDisposed: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "REAL-TIME STAGE"
) {
    val context = LocalContext.current
    val aspect = renderConfig.resolution.aspectRatioFloat

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            setBackgroundColor(android.graphics.Color.BLACK)
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: android.webkit.RenderProcessGoneDetail?
                ): Boolean {
                    // Prevent crash if cloud emulator Mesa / OpenGL render node crashes or restarts
                    return true
                }
            }
            addJavascriptInterface(bridge, "AndroidBridge")
        }
    }

    LaunchedEffect(webView) {
        onWebViewReady(webView)
    }

    // Full load whenever resolution, fps, duration, or bitrate changes
    LaunchedEffect(
        renderConfig.resolution,
        renderConfig.fps,
        renderConfig.durationSeconds,
        renderConfig.quality.bitrateBps
    ) {
        val html = EngineHtmlBuilder.buildHtml(context, userCode, renderConfig)
        webView.loadDataWithBaseURL("https://local.codemotion/", html, "text/html", "UTF-8", null)
    }

    // Fast, flicker-free live code reload whenever reloadTrigger updates
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0L) {
            val quoted = JSONObject.quote(userCode)
            webView.evaluateJavascript(
                "if (window.reloadPreviewWithCode) { window.reloadPreviewWithCode($quoted); } else { window.location.reload(); }",
                null
            )
        }
    }

    DisposableEffect(webView) {
        onDispose {
            try {
                onWebViewDisposed?.invoke()
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.clearHistory()
                webView.removeAllViews()
                webView.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = SkyGlow
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${renderConfig.resolution.width}×${renderConfig.resolution.height}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = ElectricCyan
                            )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val quoted = JSONObject.quote(userCode)
                        webView.evaluateJavascript(
                            "if (window.reloadPreviewWithCode) { window.reloadPreviewWithCode($quoted); } else { window.location.reload(); }",
                            null
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Preview",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Canvas stage container preserving target aspect ratio
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val clampedAspect = aspect.coerceIn(0.45f, 2.2f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(clampedAspect)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                ) {
                    AndroidView(
                        factory = {
                            (webView.parent as? ViewGroup)?.removeView(webView)
                            webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
