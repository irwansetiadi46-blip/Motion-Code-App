package com.example.engine

import android.webkit.JavascriptInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface BridgeEvent {
    data class Progress(
        val currentFrame: Int,
        val totalFrames: Int,
        val progress: Float,
        val status: String
    ) : BridgeEvent

    data class StreamStart(
        val filename: String,
        val width: Int,
        val height: Int,
        val durationSec: Float
    ) : BridgeEvent

    data class StreamChunk(val base64Chunk: String) : BridgeEvent

    data class StreamEnd(val totalBytes: Long) : BridgeEvent

    data class Error(val message: String) : BridgeEvent

    data class PreviewLoaded(val width: Int, val height: Int) : BridgeEvent

    data class Log(val message: String) : BridgeEvent
}

class WebCodecsBridge(
    private val scope: CoroutineScope
) {
    private val _events = MutableSharedFlow<BridgeEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    @JavascriptInterface
    fun onProgress(currentFrame: Int, totalFrames: Int, progress: Double, status: String) {
        scope.launch(Dispatchers.Main) {
            _events.emit(
                BridgeEvent.Progress(
                    currentFrame = currentFrame,
                    totalFrames = totalFrames,
                    progress = progress.toFloat(),
                    status = status
                )
            )
        }
    }

    @JavascriptInterface
    fun onStartStream(filename: String, width: Int, height: Int, durationSec: Double) {
        scope.launch(Dispatchers.Main) {
            _events.emit(
                BridgeEvent.StreamStart(
                    filename = filename,
                    width = width,
                    height = height,
                    durationSec = durationSec.toFloat()
                )
            )
        }
    }

    @JavascriptInterface
    fun onChunkStream(base64Chunk: String) {
        scope.launch(Dispatchers.IO) {
            _events.emit(BridgeEvent.StreamChunk(base64Chunk))
        }
    }

    @JavascriptInterface
    fun onEndStream(totalBytes: Long) {
        scope.launch(Dispatchers.Main) {
            _events.emit(BridgeEvent.StreamEnd(totalBytes))
        }
    }

    @JavascriptInterface
    fun onError(errorMessage: String) {
        scope.launch(Dispatchers.Main) {
            _events.emit(BridgeEvent.Error(errorMessage))
        }
    }

    @JavascriptInterface
    fun onPreviewLoaded(width: Int, height: Int) {
        scope.launch(Dispatchers.Main) {
            _events.emit(BridgeEvent.PreviewLoaded(width, height))
        }
    }

    @JavascriptInterface
    fun onConsoleLog(message: String) {
        scope.launch(Dispatchers.Default) {
            _events.emit(BridgeEvent.Log(message))
        }
    }
}
