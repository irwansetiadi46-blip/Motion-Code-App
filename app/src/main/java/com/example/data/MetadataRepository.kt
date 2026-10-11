package com.example.data

import android.content.Context
import com.example.model.VideoMetadata
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class MetadataRepository(private val context: Context) {

    private val storageFile: File
        get() = File(context.filesDir, "generated_metadata_history.json")

    @Synchronized
    fun getAllMetadata(): List<VideoMetadata> {
        return try {
            if (!storageFile.exists()) return emptyList()
            val text = storageFile.readText()
            if (text.isBlank()) return emptyList()
            val jsonArray = JSONArray(text)
            val list = mutableListOf<VideoMetadata>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    VideoMetadata(
                        filename = obj.optString("filename", ""),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        keywords = obj.optString("keywords", ""),
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.createdAtMillis }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @Synchronized
    fun saveAll(list: List<VideoMetadata>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("filename", item.filename)
                    put("title", item.title)
                    put("description", item.description)
                    put("keywords", item.keywords)
                    put("createdAtMillis", item.createdAtMillis)
                }
                jsonArray.put(obj)
            }
            storageFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun addMetadata(item: VideoMetadata) {
        val current = getAllMetadata().toMutableList()
        current.removeAll { it.id == item.id }
        current.add(0, item)
        saveAll(current)
    }

    @Synchronized
    fun updateMetadata(item: VideoMetadata) {
        val current = getAllMetadata().toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
            saveAll(current)
        }
    }

    @Synchronized
    fun deleteMetadata(id: String) {
        val current = getAllMetadata().toMutableList()
        current.removeAll { it.id == id }
        saveAll(current)
    }

    @Synchronized
    fun clearAll() {
        try {
            if (storageFile.exists()) {
                storageFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
