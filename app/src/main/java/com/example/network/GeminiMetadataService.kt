package com.example.network

import android.util.Log
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
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    companion object {
        const val MODEL_3_1_FLASH_LITE = "gemini 3.1 flash lite"
        const val MODEL_3_5_FLASH_LITE = "gemini 3.5 Flash Lite"
        const val MODEL_3_6_FLASH = "gemini 3.6 Flash"

        val AVAILABLE_MODELS = listOf(
            MODEL_3_1_FLASH_LITE,
            MODEL_3_5_FLASH_LITE,
            MODEL_3_6_FLASH
        )

        fun getModelCandidateIds(modelDisplayName: String): List<String> {
            return when (modelDisplayName.trim().lowercase()) {
                "gemini 3.1 flash lite", "gemini-3.1-flash-lite", "gemini-3.1-flash-lite-preview" ->
                    listOf("gemini-3.1-flash-lite", "gemini-3.1-flash-lite-preview", "gemini-flash-latest")

                "gemini 3.5 flash lite", "gemini-3.5-flash-lite", "gemini-3.5-flash-lite-preview" ->
                    listOf("gemini-3.5-flash-lite", "gemini-3.5-flash-lite-preview", "gemini-3.1-flash-lite")

                "gemini 3.6 flash", "gemini-3.6-flash", "gemini-3.6-flash-preview" ->
                    listOf("gemini-3.6-flash", "gemini-3.6-flash-preview", "gemini-flash-latest")

                else ->
                    listOf("gemini-3.1-flash-lite", "gemini-3.1-flash-lite-preview", "gemini-flash-latest")
            }
        }
    }

    suspend fun generateMetadataWithFailover(
        codeSnippet: String,
        selectedModel: String = MODEL_3_1_FLASH_LITE,
        presetName: String = "Code Motion Animation",
        repository: ApiKeyRepository,
        onFailover: ((oldKeyMasked: String, newKeyMasked: String) -> Unit)? = null
    ): VideoMetadata = withContext(Dispatchers.IO) {
        val keys = repository.getApiKeys()
        if (keys.isEmpty()) {
            val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }
            if (buildKey.isNotBlank() && buildKey != "your_api_key_here" && buildKey != "your_gemini_api_key_here") {
                val result = executeGeminiCall(buildKey, codeSnippet, selectedModel, presetName)
                if (result != null) return@withContext result
            }
            return@withContext generateCodeAwareMetadata(codeSnippet, presetName)
        }

        val maxAttempts = keys.size
        var attempts = 0
        while (attempts < maxAttempts) {
            val currentKey = repository.getActiveApiKey()
            if (currentKey.isNullOrBlank()) break

            val result = executeGeminiCall(currentKey, codeSnippet, selectedModel, presetName)
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

        // All keys exhausted, generate high-fidelity code-aware metadata
        generateCodeAwareMetadata(codeSnippet, presetName)
    }

    private fun executeGeminiCall(
        apiKey: String,
        codeSnippet: String,
        selectedModel: String,
        presetName: String
    ): VideoMetadata? {
        val candidateModelIds = getModelCandidateIds(selectedModel)

        for (modelId in candidateModelIds) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

                val prompt = """
                    You are a professional microstock footage curator and metadata specialist (for Adobe Stock, Shutterstock, Pond5, Getty).
                    Analyze this REAL-TIME JAVASCRIPT ANIMATION CODE that is currently being rendered into a commercial motion graphics video:

                    ================ REAL-TIME CANVAS CODE ================
                    ${codeSnippet.take(5000)}
                    ======================================================

                    Carefully inspect what this exact code visually renders on HTML5 Canvas:
                    - Visual Geometry & Elements: (particles, mathematical curves, waveforms, matrix columns, 3d projection, nodes, circuits, spirals, geometric meshes, etc.)
                    - Color Palette: (exact colors, hex codes, rgba/hsla hues, gradients, neon glow, cyber, dark mode or light mode)
                    - Motion Dynamics: (pulse, oscillation, radial burst, rotation, particle drift, fluid waves, speed, looping dynamics)
                    - Commercial Stock Video Theme: (abstract technology, artificial intelligence, cybernetic security, creative coding, scientific simulation, data visualization, futuristic loop background)

                    STRICT REQUIREMENTS:
                    1. "filename":
                       - In English.
                       - Maximum 5 words (strictly 5 words or fewer).
                       - Short, descriptive, commercial title without punctuation (e.g. "Neon Glowing Cyber Tunnel Loop").
                    2. "title":
                       - In English.
                       - Minimum 5 words, Maximum 90 characters.
                       - Accurately describe the real visual elements and motion produced by this code.
                       - High commercial search value. No generic filler terms.
                    3. "description":
                       - In English.
                       - Exactly 1 complete sentence ending with a period.
                       - Maximum 140 characters.
                       - Accurately describe the visual scene and movement seen in this animation.
                    4. "keywords":
                       - Exactly between 35 and 45 unique keywords.
                       - ALL strictly lowercase letters (no uppercase).
                       - NO punctuation inside keywords.
                       - Separated ONLY by commas and spaces.
                       - Must include specific keywords representing the visual shapes, colors, animation effects, and commercial stock themes found in the code.

                    OUTPUT FORMAT:
                    Return ONLY a valid JSON object without markdown fences:
                    {
                      "filename": "...",
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
                        put("temperature", 0.4)
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
                    val errBody = response.body?.string() ?: ""
                    Log.w("GeminiMetadataService", "Model $modelId failed HTTP ${response.code}: $errBody")
                    continue // try next candidate model id
                }

                val responseBodyString = response.body?.string() ?: continue
                val jsonRoot = JSONObject(responseBodyString)
                val candidates = jsonRoot.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

                if (rawText.isBlank()) continue

                val cleanJsonString = rawText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val parsedMeta = JSONObject(cleanJsonString)
                var rawFilename = parsedMeta.optString("filename", "").trim()
                var title = parsedMeta.optString("title", "").trim()
                var desc = parsedMeta.optString("description", "").trim()
                var keywords = parsedMeta.optString("keywords", "").trim()

                // Enforce max 5 words for filename
                val filenameWords = (if (rawFilename.isNotBlank()) rawFilename else title)
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .take(MetadataValidator.FILENAME_MAX_WORDS)
                val finalFilename = if (filenameWords.isNotEmpty()) {
                    filenameWords.joinToString(" ")
                } else {
                    "Code Motion Animation Loop"
                }

                if (title.length > MetadataValidator.TITLE_MAX_CHARS) {
                    title = title.take(MetadataValidator.TITLE_MAX_CHARS).substringBeforeLast(" ")
                }
                if (desc.length > MetadataValidator.DESC_MAX_CHARS) {
                    desc = desc.take(MetadataValidator.DESC_MAX_CHARS).substringBeforeLast(" ") + "."
                }
                if (!desc.endsWith(".")) desc += "."

                keywords = MetadataValidator.cleanAndDeduplicateKeywords(keywords)

                val kwList = keywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
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

                return VideoMetadata(
                    filename = finalFilename,
                    title = title.ifBlank { "Dynamic Code Motion Programming Visual Animation Background" },
                    description = desc.ifBlank { "Seamless creative coding visual animation loop with programmatic motion graphics." },
                    keywords = finalKeywords
                )
            } catch (e: Exception) {
                Log.e("GeminiMetadataService", "Error calling $modelId: ${e.message}")
            }
        }
        return null
    }

    fun maskKey(key: String): String {
        return if (key.length <= 10) "Key-***" else "${key.take(6)}...${key.takeLast(4)}"
    }

    /**
     * Highly dynamic metadata generator that extracts exact colors, geometric primitives,
     * and motion structures directly from the real-time code snippet.
     */
    fun generateCodeAwareMetadata(codeSnippet: String, presetName: String): VideoMetadata {
        val lowerCode = codeSnippet.lowercase()

        // Detect colors from code
        val detectedColors = mutableListOf<String>()
        if (lowerCode.contains("cyan") || lowerCode.contains("00ffff") || lowerCode.contains("38bdf8") || lowerCode.contains("00e5ff")) detectedColors.add("cyan")
        if (lowerCode.contains("green") || lowerCode.contains("00ff00") || lowerCode.contains("10b981") || lowerCode.contains("00e676")) detectedColors.add("green")
        if (lowerCode.contains("blue") || lowerCode.contains("0000ff") || lowerCode.contains("3b82f6") || lowerCode.contains("2979ff")) detectedColors.add("blue")
        if (lowerCode.contains("purple") || lowerCode.contains("magenta") || lowerCode.contains("a855f7") || lowerCode.contains("d500f9")) detectedColors.add("purple")
        if (lowerCode.contains("orange") || lowerCode.contains("ff6d00") || lowerCode.contains("f97316")) detectedColors.add("orange")
        if (lowerCode.contains("red") || lowerCode.contains("ff0000") || lowerCode.contains("f43f5e")) detectedColors.add("red")
        if (lowerCode.contains("gold") || lowerCode.contains("yellow") || lowerCode.contains("ffd600")) detectedColors.add("gold")
        val colorPrefix = if (detectedColors.isNotEmpty()) detectedColors.joinToString(" ").replaceFirstChar { it.uppercase() } + " " else "Futuristic "

        // Detect visual structure
        val isMatrix = lowerCode.contains("matrix") || lowerCode.contains("binary") || lowerCode.contains("rain") || lowerCode.contains("char")
        val isNeural = lowerCode.contains("neural") || lowerCode.contains("node") || lowerCode.contains("connect") || lowerCode.contains("synapse")
        val isParticles = lowerCode.contains("particle") || lowerCode.contains("particles") || lowerCode.contains("dot") || lowerCode.contains("arc(")
        val isWave = lowerCode.contains("sin(") || lowerCode.contains("cos(") || lowerCode.contains("wave") || lowerCode.contains("frequency")
        val isGrid = lowerCode.contains("grid") || lowerCode.contains("box") || lowerCode.contains("cube") || lowerCode.contains("lineto")
        val isCyber = lowerCode.contains("cyber") || lowerCode.contains("hex") || lowerCode.contains("terminal")

        val title = when {
            isMatrix -> "${colorPrefix}Digital Binary Matrix Stream Cyber Code Animation Background"
            isNeural -> "${colorPrefix}Neural Network Connecting Artificial Intelligence Nodes Graphic"
            isWave -> "${colorPrefix}Mathematical Sine Wave Dynamic Frequency Motion Graphics Loop"
            isParticles -> "${colorPrefix}Floating Particle Network Swarm Generative Code Motion Graphic"
            isGrid -> "${colorPrefix}3D Geometric Grid Perspective Abstract Code Motion Graphic"
            isCyber -> "${colorPrefix}High Tech Cybernetic System Data Stream Motion Background"
            else -> "${colorPrefix}Creative Coding Canvas Particle Motion Graphics Loop Background"
        }.take(MetadataValidator.TITLE_MAX_CHARS)

        val description = when {
            isMatrix -> "Seamless flowing digital binary code syntax streams cascade across a dark futuristic grid."
            isNeural -> "Dynamic artificial intelligence neural nodes pulse and interconnect across digital mathematical space."
            isWave -> "Harmonic mathematical sine waves oscilate dynamically creating an elegant glowing particle flow."
            isParticles -> "Organic particles drift and react smoothly in an infinite generative code animation loop."
            isGrid -> "Hypnotic geometric grid perspective accelerates through a high tech cyberspace digital plane."
            else -> "Smooth creative coding canvas visual animation rendering dynamic glowing elements seamlessly."
        }.take(MetadataValidator.DESC_MAX_CHARS)

        // Filename: max 5 words
        val filenameWords = when {
            isMatrix -> listOf("Binary", "Matrix", "Code", "Stream", "Loop")
            isNeural -> listOf("Neural", "Network", "AI", "Nodes", "Loop")
            isWave -> listOf("Mathematical", "Sine", "Wave", "Flow", "Loop")
            isParticles -> listOf("Particle", "Network", "Swarm", "Code", "Loop")
            isGrid -> listOf("3D", "Geometric", "Grid", "Perspective", "Loop")
            isCyber -> listOf("Cybernetic", "Data", "Stream", "Motion", "Loop")
            else -> title.split(Regex("\\s+")).filter { it.isNotBlank() }.take(5)
        }.take(MetadataValidator.FILENAME_MAX_WORDS)
        val filename = filenameWords.joinToString(" ")

        val extractedKeywords = mutableListOf<String>()
        detectedColors.forEach { extractedKeywords.add(it) }
        if (isMatrix) extractedKeywords.addAll(listOf("matrix", "binary", "rain", "stream", "cyberpunk", "hacker", "cryptography"))
        if (isNeural) extractedKeywords.addAll(listOf("neural", "network", "ai", "nodes", "intelligence", "synapse", "algorithm"))
        if (isWave) extractedKeywords.addAll(listOf("wave", "sine", "oscillation", "frequency", "harmonic", "fluid", "flow"))
        if (isParticles) extractedKeywords.addAll(listOf("particles", "swarm", "glow", "dots", "sparks", "floating", "generative"))
        if (isGrid) extractedKeywords.addAll(listOf("grid", "perspective", "geometry", "3d", "vector", "mesh", "wireframe"))
        if (isCyber) extractedKeywords.addAll(listOf("cyber", "cybersecurity", "terminal", "system", "interface", "hud", "data"))

        val universalKeywords = listOf(
            "programming", "code", "syntax", "javascript", "canvas", "animation",
            "motion graphics", "loop", "seamless", "background", "technology", "abstract",
            "developer", "software", "digital art", "creative coding", "4k", "full hd",
            "dark", "futuristic", "sci fi", "tech", "data visualization", "screen", "visuals"
        )

        val allKw = (extractedKeywords + universalKeywords)
            .map { it.trim().lowercase() }
            .distinct()
            .take(40)
            .joinToString(", ")

        return VideoMetadata(
            filename = filename,
            title = title,
            description = description,
            keywords = allKw
        )
    }

    /**
     * Backward compatible fallback generator
     */
    fun generateFallbackMetadata(presetName: String, codeSnippet: String): VideoMetadata {
        return generateCodeAwareMetadata(codeSnippet, presetName)
    }
}
