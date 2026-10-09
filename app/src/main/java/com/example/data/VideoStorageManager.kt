package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.SavedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoStorageManager(private val context: Context) {

    val videoDir: File
        get() {
            val dir = File(context.filesDir, "videos")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun getSavedVideos(): List<SavedVideo> {
        val files = videoDir.listFiles { file -> file.isFile && file.name.endsWith(".mp4") }
            ?: return emptyList()

        return files.sortedByDescending { it.lastModified() }.map { file ->
            SavedVideo(
                id = file.nameWithoutExtension,
                file = file,
                title = file.name,
                width = 1920,
                height = 1080,
                durationSeconds = 6.0f,
                fileSizeBytes = file.length(),
                createdAtMillis = file.lastModified()
            )
        }
    }

    fun createNewVideoFile(filenamePrefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "${filenamePrefix}_${timestamp}.mp4"
        return File(videoDir, name)
    }

    /**
     * Downloads video MP4 into device's local memory at "Download/Code Motion Video" folder.
     * Reports simulated/actual chunk transfer progress (0.0 .. 1.0) for visual feedback.
     */
    suspend fun downloadToCodeMotionFolder(
        videoFile: File,
        onProgress: suspend (Float) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val folderRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/Code Motion Video"
            val totalBytes = maxOf(videoFile.length(), 1L)

            // 1. Ensure physical fallback directory exists
            try {
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Code Motion Video")
                if (!publicDir.exists()) publicDir.mkdirs()
            } catch (e: Exception) {
                // Ignore fallback creation error on newer Android scoped storage
            }

            // 2. Prepare MediaStore entry
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, videoFile.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, folderRelativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val targetUri = resolver.insert(collectionUri, contentValues) ?: return@withContext null

            // 3. Stream bytes with progress reporting
            resolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(videoFile).use { inputStream ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesCopied = 0L
                    var read: Int

                    onProgress(0.05f)

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesCopied += read
                        val progress = (bytesCopied.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.98f)
                        onProgress(progress)
                        // Smooth pacing for small files so user perceives download feedback
                        delay(12)
                    }
                    outputStream.flush()
                }
            }

            // 4. Mark complete on Android Q+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }

            onProgress(1.0f)
            targetUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveToDeviceGallery(videoFile: File): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Code Motion Video")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues) ?: return@withContext null

            resolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                videoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareVideo(videoFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                videoFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Code Motion Video - ${videoFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Video MP4").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteVideo(videoFile: File): Boolean {
        return if (videoFile.exists()) videoFile.delete() else false
    }
}
