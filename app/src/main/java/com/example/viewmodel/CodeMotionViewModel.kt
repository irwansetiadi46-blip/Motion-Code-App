package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
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

    private val _savedVideos = MutableStateFlow<List<SavedVideo>>(emptyList())
    val savedVideos: StateFlow<List<SavedVideo>> = _savedVideos.asStateFlow()

    private val _selectedVideoForPlayback = MutableStateFlow<SavedVideo?>(null)
    val selectedVideoForPlayback: StateFlow<SavedVideo?> = _selectedVideoForPlayback.asStateFlow()

    private val _activeTab = MutableStateFlow(NavigationTab.STUDIO)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _reloadPreviewTrigger = MutableStateFlow(0L)
    val reloadPreviewTrigger: StateFlow<Long> = _reloadPreviewTrigger.asStateFlow()

    private var currentOutputVideoFile: File? = null
    private var currentStreamFilename: String = ""
    private var currentStreamW: Int = 1920
    private var currentStreamH: Int = 1080
    private var currentStreamDuration: Float = 6.0f

    val bridge = WebCodecsBridge(viewModelScope)

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

                    is BridgeEvent.StreamStart -> {
                        currentStreamFilename = event.filename
                        currentStreamW = event.width
                        currentStreamH = event.height
                        currentStreamDuration = event.durationSec
                        currentOutputVideoFile = storageManager.createNewVideoFile(event.filename)
                        _renderState.value = RenderState.Finalizing("Menerima data MP4...")
                    }

                    is BridgeEvent.StreamChunk -> {
                        currentOutputVideoFile?.let { file ->
                            storageManager.appendChunkToFile(file, event.base64Chunk)
                        }
                    }

                    is BridgeEvent.StreamEnd -> {
                        val file = currentOutputVideoFile
                        if (file != null && file.exists() && file.length() > 0) {
                            _renderState.value = RenderState.Success(
                                videoFile = file,
                                width = currentStreamW,
                                height = currentStreamH,
                                durationSeconds = currentStreamDuration,
                                fileSizeBytes = file.length()
                            )
                            loadSavedVideos()
                            val saved = SavedVideo(
                                id = file.nameWithoutExtension,
                                file = file,
                                title = file.name,
                                width = currentStreamW,
                                height = currentStreamH,
                                durationSeconds = currentStreamDuration,
                                fileSizeBytes = file.length(),
                                createdAtMillis = System.currentTimeMillis()
                            )
                            _selectedVideoForPlayback.value = saved
                        } else {
                            _renderState.value = RenderState.Error("Gagal menulis file video (file kosong).")
                        }
                    }

                    is BridgeEvent.Error -> {
                        _renderState.value = RenderState.Error(event.message)
                    }

                    is BridgeEvent.PreviewLoaded -> {
                        // Preview successfully rendered
                    }

                    is BridgeEvent.Log -> {
                        // Debug log from WebView
                    }
                }
            }
        }
    }

    fun updateCode(code: String) {
        _userCode.value = code
    }

    fun applyCodeToPreview() {
        _reloadPreviewTrigger.update { it + 1 }
    }

    fun selectPreset(preset: VideoPreset) {
        _selectedPreset.value = preset
        _userCode.value = preset.code
        // Match default duration & fps if wanted
        _renderConfig.update {
            it.copy(
                durationSeconds = preset.defaultDuration,
                fps = preset.defaultFps
            )
        }
        _reloadPreviewTrigger.update { it + 1 }
    }

    fun updateResolution(resolution: VideoResolution) {
        _renderConfig.update { it.copy(resolution = resolution) }
        _reloadPreviewTrigger.update { it + 1 }
    }

    fun updateDuration(duration: Int) {
        val clamped = duration.coerceIn(3, 60)
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

    fun triggerRender(webView: WebView?) {
        if (webView == null) {
            _renderState.value = RenderState.Error("WebView engine belum terhubung.")
            return
        }
        _renderState.value = RenderState.Preparing("Menginisialisasi pipeline render...")
        webView.evaluateJavascript("if (window.startVideoRender) { window.startVideoRender(); } else { window.AndroidBridge.onError('Fungsi startVideoRender tidak tersedia.'); }", null)
    }

    fun cancelOrDismissRenderState() {
        _renderState.value = RenderState.Idle
    }

    fun loadSavedVideos() {
        viewModelScope.launch {
            _savedVideos.value = storageManager.getSavedVideos()
        }
    }

    fun selectVideoForPlayback(video: SavedVideo?) {
        _selectedVideoForPlayback.value = video
    }

    fun saveVideoToGallery(video: SavedVideo) {
        viewModelScope.launch {
            val uri = storageManager.saveToDeviceGallery(video.file)
            val msg = if (uri != null) {
                "Berhasil disimpan ke Galeri (Movies/CodeMotion)"
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
            loadSavedVideos()
            Toast.makeText(getApplication(), "Video dihapus", Toast.LENGTH_SHORT).show()
        }
    }
}
