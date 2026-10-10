package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CsvExporter {

    suspend fun saveCsvToDownloads(
        context: Context,
        videoFilename: String,
        metadata: VideoMetadata
    ): Uri? = withContext(Dispatchers.IO) {
        val baseName = videoFilename.removeSuffix(".mp4")
        val csvFilename = "${baseName}_metadata.csv"
        val csvContent = metadata.toFullCsv(videoFilename)

        try {
            val resolver = context.contentResolver
            val folderRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/Code Motion Video"

            // Ensure physical fallback directory exists
            try {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Code Motion Video"
                )
                if (!publicDir.exists()) publicDir.mkdirs()
                val fallbackFile = File(publicDir, csvFilename)
                FileOutputStream(fallbackFile).use { it.write(csvContent.toByteArray()) }
            } catch (_: Exception) {}

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, csvFilename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, folderRelativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val targetUri = resolver.insert(collectionUri, contentValues) ?: return@withContext null

            resolver.openOutputStream(targetUri)?.use { outputStream: OutputStream ->
                outputStream.write(csvContent.toByteArray())
                outputStream.flush()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }

            targetUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsv(
        context: Context,
        videoFilename: String,
        metadata: VideoMetadata
    ) {
        try {
            val baseName = videoFilename.removeSuffix(".mp4")
            val csvFilename = "${baseName}_metadata.csv"
            val csvContent = metadata.toFullCsv(videoFilename)

            val cacheFile = File(context.cacheDir, csvFilename)
            cacheFile.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Stock Metadata CSV - $videoFilename")
                putExtra(Intent.EXTRA_TEXT, "Metadata footage video untuk $videoFilename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Metadata CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "✓ $label berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }
}
