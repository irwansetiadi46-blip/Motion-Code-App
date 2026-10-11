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
import com.example.model.MicrostockPlatform
import com.example.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CsvExporter {

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun getMp4Filename(metadata: VideoMetadata, fallback: String = "Vid-WM.mp4"): String {
        val raw = if (metadata.filename.isNotBlank()) metadata.filename.trim() else fallback.trim()
        val safeRaw = raw.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return if (!safeRaw.endsWith(".mp4", ignoreCase = true)) "$safeRaw.mp4" else safeRaw
    }

    /**
     * Menghasilkan teks CSV sesuai standar Adobe Stock:
     * Header: Filename,Title,Keywords,Category,Releases
     */
    fun buildAdobeStockCsv(items: List<VideoMetadata>, defaultFilename: String = "Vid-WM.mp4"): String {
        val header = "Filename,Title,Keywords,Category,Releases"
        val rows = items.map { item ->
            val fn = escapeCsv(getMp4Filename(item, defaultFilename))
            val title = escapeCsv(item.title.ifBlank { item.filename })
            val keywords = escapeCsv(item.cleanKeywordsString())
            val category = ""
            val releases = ""
            "$fn,$title,$keywords,$category,$releases"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    /**
     * Menghasilkan teks CSV sesuai standar resmi Shutterstock Video:
     * Tepat 4 Kolom: Filename,Description,Keywords,Categories
     * Kategori murni diambil dari hasil generate metadata AI (item.categories) tanpa kategori default.
     */
    fun buildShutterstockCsv(items: List<VideoMetadata>, defaultFilename: String = "Vid-WM.mp4"): String {
        val header = "Filename,Description,Keywords,Categories"
        val rows = items.map { item ->
            val fn = escapeCsv(getMp4Filename(item, defaultFilename))
            val desc = escapeCsv(item.description.ifBlank { item.title.ifBlank { item.filename } })
            val keywords = escapeCsv(item.cleanKeywordsString())
            val categories = escapeCsv(item.categories.trim())
            "$fn,$desc,$keywords,$categories"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    /**
     * Menghasilkan teks CSV sesuai standar Vecteezy:
     * Header: Filename,Title,Description,Keywords,License
     */
    fun buildVecteezyCsv(items: List<VideoMetadata>, defaultFilename: String = "Vid-WM.mp4"): String {
        val header = "Filename,Title,Description,Keywords,License"
        val rows = items.map { item ->
            val fn = escapeCsv(getMp4Filename(item, defaultFilename))
            val title = escapeCsv(item.title.ifBlank { item.filename })
            val desc = escapeCsv(item.description.ifBlank { item.title })
            val keywords = escapeCsv(item.cleanKeywordsString())
            val license = escapeCsv("Standard")
            "$fn,$title,$desc,$keywords,$license"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    /**
     * Menyimpan CSV ke Download/Code Motion Video untuk satu platform microstock
     */
    suspend fun saveMicrostockCsvToDownloads(
        context: Context,
        items: List<VideoMetadata>,
        platform: MicrostockPlatform,
        baseName: String = "metadata"
    ): Uri? = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext null

        val csvContent = when (platform) {
            MicrostockPlatform.ADOBE_STOCK -> buildAdobeStockCsv(items)
            MicrostockPlatform.SHUTTERSTOCK -> buildShutterstockCsv(items)
            MicrostockPlatform.VECTEEZY -> buildVecteezyCsv(items)
        }

        val cleanBaseName = baseName.removeSuffix(".mp4").replace(" ", "_")
        val csvFilename = "${platform.filePrefix}_${cleanBaseName}.csv"

        try {
            val resolver = context.contentResolver
            val folderRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/Code Motion Video"

            // Physical fallback
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

    suspend fun saveCsvToDownloads(
        context: Context,
        videoFilename: String,
        metadata: VideoMetadata
    ): Uri? = withContext(Dispatchers.IO) {
        val baseName = if (metadata.filename.isNotBlank()) {
            metadata.filename.removeSuffix(".mp4").replace(" ", "_")
        } else {
            videoFilename.removeSuffix(".mp4").replace(" ", "_")
        }
        val csvFilename = "${baseName}_metadata.csv"
        val csvContent = metadata.toFullCsv(if (metadata.filename.isNotBlank()) metadata.filename else videoFilename)

        try {
            val resolver = context.contentResolver
            val folderRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/Code Motion Video"

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

    suspend fun saveAllMetadataCsvToDownloads(
        context: Context,
        metadataList: List<VideoMetadata>,
        exportFilename: String = "all_stock_metadata.csv"
    ): Uri? = withContext(Dispatchers.IO) {
        if (metadataList.isEmpty()) return@withContext null

        val header = "Filename,Title,Description,Keywords"
        val rows = metadataList.map { it.toCsvRow() }
        val csvContent = (listOf(header) + rows).joinToString("\n")

        try {
            val resolver = context.contentResolver
            val folderRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/Code Motion Video"

            try {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Code Motion Video"
                )
                if (!publicDir.exists()) publicDir.mkdirs()
                val fallbackFile = File(publicDir, exportFilename)
                FileOutputStream(fallbackFile).use { it.write(csvContent.toByteArray()) }
            } catch (_: Exception) {}

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, exportFilename)
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

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareCsv(
        context: Context,
        videoFilename: String,
        metadata: VideoMetadata
    ) {
        try {
            val baseName = if (metadata.filename.isNotBlank()) {
                metadata.filename.removeSuffix(".mp4").replace(" ", "_")
            } else {
                videoFilename.removeSuffix(".mp4").replace(" ", "_")
            }
            val csvFilename = "${baseName}_metadata.csv"
            val tempFile = File(context.cacheDir, csvFilename)
            tempFile.writeText(metadata.toFullCsv(if (metadata.filename.isNotBlank()) metadata.filename else videoFilename))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Stock Video Metadata - $csvFilename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Bagikan Metadata CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan file CSV", Toast.LENGTH_SHORT).show()
        }
    }
}
