package com.example.network

import com.example.BuildConfig
import com.example.data.ApiKeyRepository
import com.example.model.VideoMetadata
import com.example.util.MetadataValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiMetadataService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateMetadataWithFailover(
        codeSnippet: String,
        presetName: String = "Code Motion Animation",
        repository: ApiKeyRepository,
        onFailover: ((oldKeyMasked: String, newKeyMasked: String) -> Unit)? = null
    ): VideoMetadata = withContext(Dispatchers.IO) {
        val keys = repository.getApiKeys()
        if (keys.isEmpty()) {
            val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
            if (buildKey.isNotBlank() && buildKey != "your_api_key_here" && buildKey != "your_gemini_api_key_here") {
                val result = executeGeminiCall(buildKey, codeSnippet, presetName)
                if (result != null) return@withContext result
            }
            return@withContext generateFallbackMetadata(presetName, codeSnippet)
        }

        val maxAttempts = keys.size
        var attempts = 0
        while (attempts < maxAttempts) {
            val currentKey = repository.getActiveApiKey()
            if (currentKey.isNullOrBlank()) break

            val result = executeGeminiCall(currentKey, codeSnippet, presetName)
            if (result != null) {
                return@withContext result
            }

            // Error occurred with currentKey, attempt failover to next key
            val oldMasked = maskKey(currentKey)
            val rotation = repository.rotateToNextApiKey()
            if (rotation != null) {
                val newMasked = maskKey(rotation.second)
                onFailover?.invoke(oldMasked, newMasked)
            }
            attempts++
        }

        // All keys exhausted, use intelligent fallback
        generateFallbackMetadata(presetName, codeSnippet)
    }

    suspend fun generateMetadata(
        codeSnippet: String,
        presetName: String = "Code Motion Animation"
    ): VideoMetadata = withContext(Dispatchers.IO) {
        val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
        if (buildKey.isNotBlank() && buildKey != "your_api_key_here" && buildKey != "your_gemini_api_key_here") {
            val res = executeGeminiCall(buildKey, codeSnippet, presetName)
            if (res != null) return@withContext res
        }
        generateFallbackMetadata(presetName, codeSnippet)
    }

    private fun executeGeminiCall(
        apiKey: String,
        codeSnippet: String,
        presetName: String
    ): VideoMetadata? {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val prompt = """
                You are a senior motion footage metadata specialist.
                Generate optimal English metadata for a programmatic code animation video based on this context:
                Preset: "$presetName"
                Code snippet excerpt:
                ${codeSnippet.take(1200)}

                STRICT MANDATORY RULES (FAILING ANY RULE IS UNACCEPTABLE):
                1. "title":
                   - In English.
                   - Minimum 5 words.
                   - Maximum 100 characters total.
                   - Clearly describe the motion graphics and programming concept.
                2. "description":
                   - In English.
                   - Exactly 1 complete sentence ending with a period.
                   - Maximum 150 characters total.
                3. "keywords":
                   - Exactly between 35 and 45 unique keywords.
                   - ALL strictly lowercase letters.
                   - NO uppercase characters anywhere in keywords.
                   - NO duplicate keywords.
                   - Separated ONLY by commas and spaces.
                   - Relevant to coding, digital art, technology, animation, abstract motion, loop, 4k background.

                OUTPUT FORMAT:
                Return ONLY a single valid JSON object, without markdown formatting, with exact keys:
                {
                  "title": "...",
                  "description": "...",
                  "keywords": "..."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return null
            }

            val responseBodyString = response.body?.string() ?: return null
            val jsonRoot = JSONObject(responseBodyString)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (rawText.isBlank()) return null

            val cleanJsonString = rawText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedMeta = JSONObject(cleanJsonString)
            var title = parsedMeta.optString("title", "").trim()
            var desc = parsedMeta.optString("description", "").trim()
            var keywords = parsedMeta.optString("keywords", "").trim()

            if (title.length > MetadataValidator.TITLE_MAX_CHARS) {
                title = title.take(MetadataValidator.TITLE_MAX_CHARS).substringBeforeLast(" ")
            }
            if (desc.length > MetadataValidator.DESC_MAX_CHARS) {
                desc = desc.take(MetadataValidator.DESC_MAX_CHARS).substringBeforeLast(" ") + "."
            }
            if (!desc.endsWith(".")) desc += "."

            keywords = MetadataValidator.cleanAndDeduplicateKeywords(keywords)

            val kwList = keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val finalKeywords = if (kwList.size < MetadataValidator.KEYWORDS_MIN) {
                val additional = listOf(
                    "programming", "technology", "abstract", "digital art", "animation",
                    "motion graphics", "developer", "coding", "software", "screen background",
                    "creative coding", "cyberpunk", "futuristic", "data visualization", "loop"
                )
                (kwList + additional).distinct().take(45).joinToString(", ")
            } else if (kwList.size > MetadataValidator.KEYWORDS_MAX) {
                kwList.take(MetadataValidator.KEYWORDS_MAX).joinToString(", ")
            } else {
                keywords
            }

            VideoMetadata(
                title = title.ifBlank { "Futuristic Code Motion Programming Syntax Animation Background" },
                description = desc.ifBlank { "Seamless programming code syntax animation with futuristic digital particles." },
                keywords = finalKeywords
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun maskKey(key: String): String {
        return if (key.length <= 10) "Key-***" else "${key.take(6)}...${key.takeLast(4)}"
    }

    /**
     * Fallback generator ensuring 100% rule compliance without internet / API key.
     */
    fun generateFallbackMetadata(presetName: String, codeSnippet: String): VideoMetadata {
        val lowerCode = codeSnippet.lowercase()
        val isMatrix = lowerCode.contains("matrix") || presetName.contains("Matrix", ignoreCase = true)
        val isNeural = lowerCode.contains("neural") || lowerCode.contains("node") || presetName.contains("Neural", ignoreCase = true)
        val isTerminal = lowerCode.contains("terminal") || lowerCode.contains("prompt") || presetName.contains("Terminal", ignoreCase = true)

        val title = when {
            isMatrix -> "Digital Matrix Rain Binary Code Stream Programming Motion Background"
            isNeural -> "Neural Network Connecting Nodes Digital Artificial Intelligence Motion Graphic"
            isTerminal -> "Futuristic Terminal Shell Command Line Programming Syntax Animation"
            else -> "Abstract Programming Code Syntax Motion Graphics Background Loop"
        }.take(MetadataValidator.TITLE_MAX_CHARS)

        val description = when {
            isMatrix -> "Seamless glowing binary code stream falling over dark futuristic cyber grid."
            isNeural -> "Interactive artificial intelligence neural network nodes pulsing in digital space."
            isTerminal -> "High tech hacker terminal executing programmatic scripts and animated syntax."
            else -> "Creative coding visual animation with dynamic glowing particles and syntax."
        }.take(MetadataValidator.DESC_MAX_CHARS)

        val baseKeywords = when {
            isMatrix -> listOf(
                "matrix", "binary", "rain", "cyberpunk", "hacker", "stream", "falling",
                "green", "glow", "programming", "code", "syntax", "javascript", "canvas",
                "developer", "software", "futuristic", "sci fi", "terminal", "dark",
                "background", "motion", "animation", "loop", "seamless", "tech",
                "technology", "data", "cyber", "screen", "digital", "abstract",
                "visual", "computer", "graphic", "4k", "full hd", "cryptography"
            )
            isNeural -> listOf(
                "neural", "network", "artificial intelligence", "ai", "machine learning",
                "nodes", "connecting", "deep learning", "algorithm", "digital", "brain",
                "synapse", "data", "science", "programming", "code", "animation",
                "motion", "futuristic", "cyber", "technology", "graph", "particles",
                "glowing", "abstract", "background", "loop", "tech", "computer",
                "canvas", "software", "developer", "visuals", "blue", "cyan", "science"
            )
            else -> listOf(
                "programming", "code", "syntax", "motion", "animation", "javascript",
                "developer", "computer", "software", "coding", "abstract", "technology",
                "digital", "graphic", "futuristic", "data", "background", "loop",
                "screen", "visuals", "particle", "glow", "cyber", "algorithm",
                "creative", "web development", "tech", "canvas", "interface", "display",
                "modern", "clean", "dark", "4k", "full hd", "looping", "design", "creative coding"
            )
        }

        val keywordsString = baseKeywords
            .map { it.trim().lowercase() }
            .distinct()
            .take(40)
            .joinToString(", ")

        return VideoMetadata(
            title = title,
            description = description,
            keywords = keywordsString
        )
    }
}
