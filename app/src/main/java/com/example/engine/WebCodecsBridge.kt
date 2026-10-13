package com.example.engine

import android.content.Context
import android.util.Base64
import android.webkit.JavascriptInterface
import com.example.data.VideoStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed interface BridgeEvent {
    data class Progress(
        val currentFrame: Int,
        val totalFrames: Int,
        val progress: Float,
        val status: String
    ) : BridgeEvent

    data class Success(
        val videoFile: File,
        val width: Int,
        val height: Int,
        val durationSec: Float,
        val fileSizeBytes: Long
    ) : BridgeEvent

    data class ImageSuccess(
        val imageFile: File,
        val width: Int,
        val height: Int,
        val format: com.example.model.ImageFormat,
        val fileSizeBytes: Long
    ) : BridgeEvent

    data class Error(val message: String) : BridgeEvent

    data class PreviewLoaded(val width: Int, val height: Int) : BridgeEvent

    data class Log(val message: String) : BridgeEvent
}

class WebCodecsBridge(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val storageManager = VideoStorageManager(context)
    private val streamLock = Any()

    private var activeOutputStream: FileOutputStream? = null
    private var activeOutputFile: File? = null
    private var streamFilename: String = ""
    private var streamW: Int = 1920
    private var streamH: Int = 1080
    private var streamDuration: Float = 6f

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
    fun onStartStream(filename: String, width: Int, height: Int, durationSec: Double, totalBytes: Long) {
        synchronized(streamLock) {
            try {
                // Close previous if any
                activeOutputStream?.close()
                val file = storageManager.createNewVideoFile(filename)
                activeOutputFile = file
                activeOutputStream = FileOutputStream(file)
                streamFilename = filename
                streamW = width
                streamH = height
                streamDuration = durationSec.toFloat()
            } catch (e: Exception) {
                onError("Gagal menyiapkan file output: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun onChunkStream(base64Chunk: String) {
        synchronized(streamLock) {
            try {
                val bytes = Base64.decode(base64Chunk, Base64.DEFAULT)
                activeOutputStream?.write(bytes)
            } catch (e: Exception) {
                onError("Gagal menulis chunk ke file: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun onEndStream(totalBytes: Long) {
        synchronized(streamLock) {
            try {
                activeOutputStream?.flush()
                activeOutputStream?.close()
                activeOutputStream = null

                val file = activeOutputFile
                if (file != null && file.exists() && file.length() > 0) {
                    val finalFile = file
                    val width = streamW
                    val height = streamH
                    val duration = streamDuration
                    val size = file.length()

                    scope.launch(Dispatchers.Main) {
                        _events.emit(
                            BridgeEvent.Success(
                                videoFile = finalFile,
                                width = width,
                                height = height,
                                durationSec = duration,
                                fileSizeBytes = size
                            )
                        )
                    }
                } else {
                    onError("File video kosong atau gagal disimpan.")
                }
            } catch (e: Exception) {
                onError("Gagal menutup file: ${e.message}")
            }
        }
    }

    private var streamImageFormat: com.example.model.ImageFormat? = null

    @JavascriptInterface
    fun onStartImageStream(filename: String, formatStr: String, width: Int, height: Int) {
        synchronized(streamLock) {
            try {
                activeOutputStream?.close()
                val format = if (formatStr.equals("jpg", ignoreCase = true) || formatStr.equals("jpeg", ignoreCase = true)) {
                    com.example.model.ImageFormat.JPG
                } else {
                    com.example.model.ImageFormat.PNG
                }
                streamImageFormat = format
                val file = storageManager.createImageFile(filename, format)
                activeOutputFile = file
                activeOutputStream = FileOutputStream(file)
                streamFilename = filename
                streamW = width
                streamH = height
            } catch (e: Exception) {
                onError("Gagal menyiapkan file output gambar: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun onEndImageStream() {
        synchronized(streamLock) {
            try {
                activeOutputStream?.flush()
                activeOutputStream?.close()
                activeOutputStream = null

                val file = activeOutputFile
                val format = streamImageFormat ?: com.example.model.ImageFormat.PNG
                if (file != null && file.exists() && file.length() > 0) {
                    val finalFile = file
                    val width = streamW
                    val height = streamH
                    val size = file.length()

                    scope.launch(Dispatchers.Main) {
                        _events.emit(
                            BridgeEvent.ImageSuccess(
                                imageFile = finalFile,
                                width = width,
                                height = height,
                                format = format,
                                fileSizeBytes = size
                            )
                        )
                    }
                } else {
                    onError("File gambar kosong atau gagal disimpan.")
                }
            } catch (e: Exception) {
                onError("Gagal menutup file gambar: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun onSaveImage(dataUrlOrBase64: String, formatStr: String, width: Int, height: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                val format = if (formatStr.equals("jpg", ignoreCase = true) || formatStr.equals("jpeg", ignoreCase = true)) {
                    com.example.model.ImageFormat.JPG
                } else {
                    com.example.model.ImageFormat.PNG
                }
                val cleanBase64 = if (dataUrlOrBase64.contains(",")) {
                    dataUrlOrBase64.substringAfter(",")
                } else {
                    dataUrlOrBase64
                }
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val filename = "Img-WM_${width}x${height}_${System.currentTimeMillis()}"
                val file = storageManager.createImageFile(filename, format)
                FileOutputStream(file).use { it.write(bytes) }

                withContext(Dispatchers.Main) {
                    _events.emit(
                        BridgeEvent.ImageSuccess(
                            imageFile = file,
                            width = width,
                            height = height,
                            format = format,
                            fileSizeBytes = file.length()
                        )
                    )
                }
            } catch (e: Exception) {
                onError("Gagal menyimpan gambar: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun onError(errorMessage: String) {
        synchronized(streamLock) {
            try {
                activeOutputStream?.close()
                activeOutputStream = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun cancelActiveRender() {
        synchronized(streamLock) {
            try {
                activeOutputStream?.close()
                activeOutputStream = null
                activeOutputFile?.let { file ->
                    if (file.exists()) file.delete()
                }
                activeOutputFile = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
