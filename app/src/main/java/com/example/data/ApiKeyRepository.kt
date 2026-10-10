package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import org.json.JSONArray

class ApiKeyRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "war_motion_api_prefs"
        private const val KEY_API_KEYS_JSON = "api_keys_json"
        private const val KEY_ACTIVE_INDEX = "active_key_index"
    }

    init {
        // App starts strictly with no API keys; user must input their own keys
    }

    fun getApiKeys(): List<String> {
        val jsonStr = prefs.getString(KEY_API_KEYS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optString(i, "").trim()
                if (item.isNotEmpty()) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveIndex(): Int {
        val keys = getApiKeys()
        if (keys.isEmpty()) return -1
        val savedIndex = prefs.getInt(KEY_ACTIVE_INDEX, 0)
        return if (savedIndex in keys.indices) savedIndex else 0
    }

    fun getActiveApiKey(): String? {
        val keys = getApiKeys()
        if (keys.isEmpty()) return null
        val idx = getActiveIndex()
        return if (idx in keys.indices) keys[idx] else keys.firstOrNull()
    }

    fun addApiKey(newKey: String): Boolean {
        val trimmed = newKey.trim()
        if (trimmed.isEmpty()) return false
        val currentKeys = getApiKeys().toMutableList()
        if (currentKeys.contains(trimmed)) return false

        currentKeys.add(trimmed)
        saveKeys(currentKeys)

        if (currentKeys.size == 1) {
            setActiveIndex(0)
        }
        return true
    }

    fun setActiveIndex(index: Int) {
        val keys = getApiKeys()
        if (index in keys.indices) {
            prefs.edit().putInt(KEY_ACTIVE_INDEX, index).apply()
        }
    }

    fun deleteApiKey(index: Int): Boolean {
        val currentKeys = getApiKeys().toMutableList()
        if (index !in currentKeys.indices) return false

        currentKeys.removeAt(index)
        saveKeys(currentKeys)

        val newActive = if (currentKeys.isEmpty()) {
            -1
        } else {
            val cur = prefs.getInt(KEY_ACTIVE_INDEX, 0)
            when {
                cur >= currentKeys.size -> currentKeys.size - 1
                cur > index -> cur - 1
                else -> cur
            }
        }
        prefs.edit().putInt(KEY_ACTIVE_INDEX, newActive).apply()
        return true
    }

    /**
     * Rotates round-robin to next available API key.
     * Returns Pair(newIndex, newKey) or null if <= 1 key.
     */
    fun rotateToNextApiKey(): Pair<Int, String>? {
        val keys = getApiKeys()
        if (keys.size <= 1) return null

        val curIndex = getActiveIndex()
        val nextIndex = (curIndex + 1) % keys.size
        setActiveIndex(nextIndex)
        return Pair(nextIndex, keys[nextIndex])
    }

    private fun saveKeys(keys: List<String>) {
        val jsonArray = JSONArray()
        keys.forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_API_KEYS_JSON, jsonArray.toString()).apply()
    }
}
