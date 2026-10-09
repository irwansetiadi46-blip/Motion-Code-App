package com.example.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PresetRepository
import com.example.data.VideoStorageManager
import com.example.engine.BridgeEvent
import com.example.engine.WebCodecsBridge
import com.example.model.QualityBitrate
import com.example.model.RenderConfig
import com.example.model.RenderState
import com.example.model.SavedVideo
import com.example.model.VideoPreset
import com.example.model.VideoResolution
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class NavigationTab(val label: String) {
    STUDIO("Editor & Live"),
    EXPORT("Export Video"),
    TEMPLATES("Presets"),
    GALLERY("Saved Videos")
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(
        val progress: Float,
        val percent: Int,
        val message: String
    ) : DownloadState
    data class Success(
        val folderName: String,
        val filename: String
    ) : DownloadState
    data class Error(val message: String) : DownloadState
}

class CodeMotionViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = VideoStorageManager(application)

    private val _userCode = MutableStateFlow(PresetRepository.PRESETS[0].code)
    val userCode: StateFlow<String> = _userCode.asStateFlow()

    private val _selectedPreset = MutableStateFlow<VideoPreset>(PresetRepository.PRESETS[0])
    val selectedPreset: StateFlow<VideoPreset> = _selectedPreset.asStateFlow()

    private val _renderConfig = MutableStateFlow(RenderConfig())
    val renderConfig: StateFlow<RenderConfig> = _renderConfig.asStateFlow()

    private val _renderState = MutableStateFlow<RenderState>(RenderState.Idle)
    val renderState: StateFlow<RenderState> = _renderState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _savedVideos = MutableStateFlow<List<SavedVideo>>(emptyList())
    val savedVideos: StateFlow<List<SavedVideo>> = _savedVideos.asStateFlow()

    private val _selectedVideoForPlayback = MutableStateFlow<SavedVideo?>(null)
    val selectedVideoForPlayback: StateFlow<SavedVideo?> = _selectedVideoForPlayback.asStateFlow()

    private val _lastRenderedVideo = MutableStateFlow<SavedVideo?>(null)
    val lastRenderedVideo: StateFlow<SavedVideo?> = _lastRenderedVideo.asStateFlow()

    private val _activeTab = MutableStateFlow(NavigationTab.STUDIO)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _reloadPreviewTrigger = MutableStateFlow(0L)
    val reloadPreviewTrigger: StateFlow<Long> = _reloadPreviewTrigger.asStateFlow()

    val bridge = WebCodecsBridge(application, viewModelScope)

    private var _boundWebView: WebView? = null
    val boundWebView: WebView? get() = _boundWebView

    fun bindWebView(wv: WebView?) {
        _boundWebView = wv
    }

    private var codeDebounceJob: Job? = null

    init {
        loadSavedVideos()
        observeBridgeEvents()
    }

    private fun observeBridgeEvents() {
        viewModelScope.launch {
            bridge.events.collect { event ->
                when (event) {
                    is BridgeEvent.Progress -> {
                        _renderState.value = RenderState.Rendering(
                            currentFrame = event.currentFrame,
                            totalFrames = event.totalFrames,
                            progress = event.progress,
                            statusText = event.status
                        )
                    }

                    is BridgeEvent.Success -> {
                        val saved = SavedVideo(
                            id = event.videoFile.nameWithoutExtension,
                            file = event.videoFile,
                            title = event.videoFile.name,
                            width = event.width,
                            height = event.height,
                            durationSeconds = event.durationSec,
                            fileSizeBytes = event.fileSizeBytes,
                            createdAtMillis = System.currentTimeMillis()
                        )

                        _renderState.value = RenderState.Success(
                            videoFile = event.videoFile,
                            width = event.width,
                            height = event.height,
                            durationSeconds = event.durationSec,
                            fileSizeBytes = event.fileSizeBytes
                        )

                        _lastRenderedVideo.value = saved
                        _selectedVideoForPlayback.value = saved
                        loadSavedVideos()

                        Toast.makeText(
                            getApplication(),
                            "✅ Render Selesai! Preview MP4 ditampilkan di bawah.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is BridgeEvent.Error -> {
                        _renderState.value = RenderState.Error(event.message)
                        Toast.makeText(getApplication(), "❌ Render Gagal: ${event.message}", Toast.LENGTH_LONG).show()
                    }

                    is BridgeEvent.PreviewLoaded -> {
                        // Preview successfully loaded in viewport
                    }

                    is BridgeEvent.Log -> {
                        // Log message
                    }
                }
            }
        }
    }

    /**
     * Updates code and automatically debounces preview refresh in real-time
     * so user sees changes live in the viewport as they type.
     */
    fun updateCode(code: String) {
        _userCode.value = code
        codeDebounceJob?.cancel()
        codeDebounceJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            _reloadPreviewTrigger.update { it + 1 }
        }
    }

    fun applyCodeToPreview() {
        codeDebounceJob?.cancel()
        _reloadPreviewTrigger.update { it + 1 }
    }

    fun selectPreset(preset: VideoPreset) {
        _selectedPreset.value = preset
        _userCode.value = preset.code
        _renderConfig.update {
            it.copy(
                durationSeconds = preset.defaultDuration,
                fps = preset.defaultFps
            )
        }
        applyCodeToPreview()
    }

    fun updateResolution(resolution: VideoResolution) {
        _renderConfig.update { it.copy(resolution = resolution) }
        _reloadPreviewTrigger.update { it + 1 }
    }

    fun updateDuration(duration: Int) {
        val clamped = duration.coerceIn(3, 30)
        _renderConfig.update { it.copy(durationSeconds = clamped) }
    }

    fun updateFps(fps: Int) {
        _renderConfig.update { it.copy(fps = fps) }
    }

    fun updateQuality(quality: QualityBitrate) {
        _renderConfig.update { it.copy(quality = quality) }
    }

    fun setActiveTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun triggerRender() {
        val wv = _boundWebView
        if (wv == null) {
            _renderState.value = RenderState.Error("Stage animasi belum siap. Buka tab Studio terlebih dahulu.")
            return
        }
        _renderState.value = RenderState.Preparing("Menyiapkan encoder...")
        try {
            wv.evaluateJavascript(
                "if (window.startVideoRender) { window.startVideoRender(); } else { window.AndroidBridge.onError('Engine belum siap.'); }",
                null
            )
        } catch (e: Exception) {
            _renderState.value = RenderState.Error("Gagal menjalankan render: ${e.message ?: "WebView tidak aktif"}")
        }
    }

    fun cancelOrDismissRenderState() {
        _renderState.value = RenderState.Idle
    }

    fun dismissDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun loadSavedVideos() {
        viewModelScope.launch {
            _savedVideos.value = storageManager.getSavedVideos()
        }
    }

    fun selectVideoForPlayback(video: SavedVideo?) {
        _selectedVideoForPlayback.value = video
    }

    /**
     * Downloads video MP4 into "Download/Code Motion Video" folder
     * with live progress indicator, spinner, and percentage.
     */
    fun downloadVideo(video: SavedVideo) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading(
                progress = 0.05f,
                percent = 5,
                message = "Memulai proses download ${video.file.name}..."
            )

            val uri = storageManager.downloadToCodeMotionFolder(video.file) { progress ->
                val pct = (progress * 100).toInt().coerceIn(0, 100)
                _downloadState.value = DownloadState.Downloading(
                    progress = progress,
                    percent = pct,
                    message = "Menyimpan ke folder Download/Code Motion Video ($pct%)..."
                )
            }

            if (uri != null) {
                _downloadState.value = DownloadState.Success(
                    folderName = "Download/Code Motion Video",
                    filename = video.file.name
                )
                Toast.makeText(
                    getApplication(),
                    "✅ Download Selesai! Tersimpan di: Download/Code Motion Video/${video.file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val isPermissionMissing = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                val errorMsg = if (isPermissionMissing) {
                    "Butuh izin penyimpanan untuk download di Android versi ini."
                } else {
                    "Gagal men-download video ke penyimpanan lokal."
                }
                _downloadState.value = DownloadState.Error(errorMsg)
                Toast.makeText(getApplication(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveVideoToGallery(video: SavedVideo) {
        viewModelScope.launch {
            val uri = storageManager.saveToDeviceGallery(video.file)
            val msg = if (uri != null) {
                "✅ Berhasil disimpan ke Galeri (Movies/Code Motion Video)"
            } else {
                "Gagal menyimpan ke Galeri"
            }
            Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show()
        }
    }

    fun shareVideo(video: SavedVideo) {
        storageManager.shareVideo(video.file)
    }

    fun deleteVideo(video: SavedVideo) {
        viewModelScope.launch {
            storageManager.deleteVideo(video.file)
            if (_selectedVideoForPlayback.value?.id == video.id) {
                _selectedVideoForPlayback.value = null
            }
            if (_lastRenderedVideo.value?.id == video.id) {
                _lastRenderedVideo.value = null
            }
            loadSavedVideos()
            Toast.makeText(getApplication(), "Video dihapus", Toast.LENGTH_SHORT).show()
        }
    }
}
