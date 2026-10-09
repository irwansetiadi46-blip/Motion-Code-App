package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.model.SavedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VideoStorageManager(private val context: Context) {

    private val videoDir: File
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
                width = 1920, // default metadata fallback
                height = 1080,
                durationSeconds = 6.0f,
                fileSizeBytes = file.length(),
                createdAtMillis = file.lastModified()
            )
        }
    }

    suspend fun createNewVideoFile(filenamePrefix: String): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "${filenamePrefix}_${timestamp}.mp4"
        File(videoDir, name)
    }

    suspend fun appendChunkToFile(file: File, base64Chunk: String) = withContext(Dispatchers.IO) {
        val bytes = Base64.decode(base64Chunk, Base64.DEFAULT)
        FileOutputStream(file, true).use { out ->
            out.write(bytes)
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
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/CodeMotion")
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
                putExtra(Intent.EXTRA_SUBJECT, "Code Animation Footage - ${videoFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Video Animasi").apply {
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
