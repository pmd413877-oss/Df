package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(GeminiApiService::class.java)

    companion object {
        // High-compatibility candidates tried in order if discovery cannot run
        private val DEFAULT_CANDIDATE_MODELS = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-2.0-flash-exp",
            "gemini-1.5-flash-8b",
            "gemini-1.5-pro",
            "gemini-pro"
        )
    }

    // Cache the verified working model for the current session
    @Volatile
    private var cachedWorkingModel: String? = null

    fun getActiveModel(): String? = cachedWorkingModel

    fun setActiveModel(model: String) {
        val clean = cleanModelName(model)
        if (clean.isNotBlank()) {
            cachedWorkingModel = clean
        }
    }

    /**
     * Resolves the active API key: user-configured key takes precedence,
     * otherwise falls back to BuildConfig.GEMINI_API_KEY if present and valid.
     */
    fun resolveApiKey(userKey: String): String {
        if (userKey.isNotBlank()) return userKey.trim()

        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildConfigKey = field.get(null) as? String ?: ""
            if (buildConfigKey.isNotBlank() && !buildConfigKey.contains("MY_GEMINI_API_KEY")) {
                buildConfigKey.trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun cleanModelName(model: String?): String {
        val trimmed = model?.trim() ?: ""
        val withoutPrefix = if (trimmed.startsWith("models/")) {
            trimmed.removePrefix("models/")
        } else {
            trimmed
        }
        return if (withoutPrefix == "gemini-2.5-flash") "gemini-2.0-flash" else withoutPrefix
    }

    /**
     * Dynamically queries the Google Generative Language API for models
     * available to this specific key that support content generation.
     */
    private suspend fun discoverSupportedModels(apiKey: String): List<String> {
        return try {
            val response = apiService.listModels(apiKey)
            if (response.isSuccessful) {
                val list = response.body()?.models ?: emptyList()
                list.filter { info ->
                    val methods = info.supportedGenerationMethods
                    methods == null || methods.contains("generateContent")
                }.map { info ->
                    cleanModelName(info.name)
                }.filter { it.isNotBlank() }
                .sortedWith { m1, m2 ->
                    // Prioritize modern flash models, then 2.0, then 1.5, then pro
                    val score1 = modelScore(m1)
                    val score2 = modelScore(m2)
                    score1.compareTo(score2)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun modelScore(modelName: String): Int {
        val lower = modelName.lowercase()
        return when {
            lower.contains("2.0-flash") && !lower.contains("exp") -> 0
            lower.contains("1.5-flash") && !lower.contains("8b") -> 1
            lower.contains("2.0-flash-exp") -> 2
            lower.contains("1.5-flash-8b") -> 3
            lower.contains("2.0") -> 4
            lower.contains("1.5-pro") -> 5
            lower.contains("flash") -> 6
            lower.contains("pro") -> 7
            lower.contains("gemini") -> 8
            else -> 9
        }
    }

    /**
     * Builds an ordered list of candidate models to attempt for the given key.
     * Ensures any provided API key automatically works without manual model configuration.
     */
    private suspend fun getCandidateModelsFor(apiKey: String, preferredModel: String? = null): List<String> {
        val candidates = mutableListOf<String>()

        // 1. Cached verified working model first
        cachedWorkingModel?.let {
            if (it.isNotBlank() && !candidates.contains(it)) candidates.add(it)
        }

        // 2. Preferred/passed model
        val cleanPreferred = preferredModel?.let { cleanModelName(it) }?.takeIf { it.isNotBlank() }
        if (cleanPreferred != null && !candidates.contains(cleanPreferred)) {
            candidates.add(cleanPreferred)
        }

        // 3. Dynamic discovery from Google API
        val discovered = discoverSupportedModels(apiKey)
        discovered.forEach { model ->
            if (!candidates.contains(model)) {
                candidates.add(model)
            }
        }

        // 4. Fallback default candidates
        DEFAULT_CANDIDATE_MODELS.forEach { model ->
            if (!candidates.contains(model)) {
                candidates.add(model)
            }
        }

        return candidates
    }

    /**
     * Tests an API key by trying available models automatically.
     * Guarantees that any valid Gemini API key will connect without requiring model selection.
     */
    suspend fun testConnection(apiKey: String, model: String? = null): GeminiResult {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return GeminiResult.Error("API key cannot be empty (API কী খালি হতে পারে না)", isAuthError = true)
        }

        val testRequest = GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello! Answer with only OK."))
                )
            ),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 10)
        )

        val candidates = getCandidateModelsFor(trimmedKey, model)
        var lastError: GeminiResult.Error? = null

        for (candidate in candidates) {
            try {
                val response = apiService.generateContent(
                    model = candidate,
                    apiKey = trimmedKey,
                    request = testRequest
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    cachedWorkingModel = candidate
                    val successMsg = if (!text.isNullOrBlank()) {
                        "সংযোগ সফল হয়েছে! Gemini প্রস্তুত (স্বয়ংক্রিয় মডেল: $candidate)।"
                    } else {
                        "সংযোগ সফল হয়েছে! Gemini প্রস্তুত।"
                    }
                    return GeminiResult.Success(successMsg, modelUsed = candidate)
                }

                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: ""

                when (errorCode) {
                    401, 403 -> {
                        // Key itself is unauthorized or invalid; don't loop needlessly
                        return GeminiResult.Error(
                            "API কী ভুল বা অননুমোদিত ($errorCode)। অনুগ্রহ করে আপনার সঠিক Google AI Studio API key দিন।",
                            isAuthError = true
                        )
                    }
                    429 -> {
                        return GeminiResult.Error(
                            "API কোটা শেষ বা রেট লিমিট অতিক্রম করেছে ($errorCode)। অনুগ্রহ করে কিছুক্ষণ পর চেষ্টা করুন।",
                            isQuotaError = true
                        )
                    }
                    404 -> {
                        // Model not supported by this key, try next candidate
                        lastError = GeminiResult.Error("Gemini মডেল '$candidate' পাওয়া যায়নি (404)।")
                        continue
                    }
                    else -> {
                        lastError = GeminiResult.Error("API Error ($errorCode): $errorBody")
                    }
                }
            } catch (e: IOException) {
                lastError = GeminiResult.Error("ইন্টারনেট সংযোগে সমস্যা হচ্ছে। আপনার ওয়াই-ফাই বা মোবাইল ডেটা চেক করে আবার চেষ্টা করুন।")
            } catch (e: Exception) {
                lastError = GeminiResult.Error("সার্ভিস ত্রুটি: ${e.localizedMessage ?: "Unknown error"}")
            }
        }

        return lastError ?: GeminiResult.Error("Gemini মডেল সংযোগ করা সম্ভব হয়নি (404)। আপনার API কী টি AI Studio তে পরীক্ষা করুন।")
    }

    /**
     * Sends a user query to Gemini and returns the assistant's reply.
     * Automatically uses the verified working model or falls back gracefully on 404.
     */
    suspend fun askGemini(
        question: String,
        userApiKey: String,
        languageCode: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        model: String? = null
    ): GeminiResult {
        val activeKey = resolveApiKey(userApiKey)
        if (activeKey.isBlank()) {
            return GeminiResult.Error(
                message = "Gemini API key পাওয়া যায়নি। অনুগ্রহ করে অ্যাপের সেটিংস থেকে আপনার Google AI Studio API key দিন।",
                isAuthError = true
            )
        }

        val languagePrompt = when (languageCode) {
            "bn" -> """
                তুমি 'জারা' (Zara AI) — ব্যবহারকারীর অত্যন্ত বন্ধুসুলভ, মার্জিত, বুদ্ধিমান এবং স্পষ্টভাষী ব্যক্তিগত এআই সহকারী।
                তোমার কথা বলা এবং টেক্সট আউটপুটের মূল নিয়মাবলী:
                ১. ভাষা সবসময় খুব পরিষ্কার, সাবলীল এবং মার্জিত হতে হবে। কোনো জটিল, কৃত্রিম বা রোবটিক শব্দ ব্যবহার না করে একদম স্বাভাবিক মানুষের মতো সহজ বাংলায় কথা বলবে।
                ২. তোমার উত্তরগুলো যেন সংক্ষিপ্ত, স্পষ্ট এবং শোনার জন্য উপযোগী হয় (Text-to-Speech friendly)। অহেতুক লম্বা বক্তৃতা পরিহার করবে।
                ৩. কোনো উত্তরের মাঝে স্টার চিহ্ন (** বা *), হ্যাশ (#), আন্ডারস্কোর (_), বুলেট পয়েন্ট বা কোড চিহ্ন লিখবে না, কারণ ভয়েসে পড়ার সময় এগুলো অদ্ভুত শোনায়। একদম স্বাভাবিক সহজ বাক্যে কথা বলবে।
                ৪. তুমি ফোন অ্যাপস ওপেন করা, ইউটিউব ও গুগলে সার্চ করা, কল স্ক্রিনিং ও মেসেজ সামলানোসহ প্রয়োজনীয় সব কাজ করে দাও। সবসময় বন্ধুসুলভ, বিনম্র ও সহায়তামূলক ভঙ্গিতে কথা বলবে।
            """.trimIndent()
            "hi" -> "You are 'Zara AI', a friendly and articulate personal voice assistant. Respond in natural, conversational, simple Hindi (हिन्दी). Keep answers concise, polite, and spoken-friendly without any markdown asterisks."
            "ar" -> "You are 'Zara AI', a friendly and articulate personal voice assistant. Respond in natural, conversational Arabic. Keep answers concise and easy to listen to without markdown asterisks."
            "es" -> "You are 'Zara AI', a friendly and articulate personal voice assistant. Respond in natural, conversational Spanish. Keep answers concise and easy to listen to without markdown asterisks."
            else -> "You are 'Zara AI', an extremely friendly, polite, and articulate personal voice assistant running natively on Android. Keep your tone natural, human-like, and warm. Avoid robotic jargon. Give concise, clear, and direct answers optimized for listening through Text-to-Speech without asterisks or markdown symbols."
        }

        val contentsList = mutableListOf<GeminiContent>()
        recentHistory.takeLast(4).forEach { (pastQ, pastA) ->
            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = pastQ))))
            contentsList.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = pastA))))
        }
        contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = question))))

        val request = GeminiGenerateContentRequest(
            contents = contentsList,
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = languagePrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 600
            )
        )

        val candidates = getCandidateModelsFor(activeKey, model)
        var lastError: GeminiResult.Error? = null

        for (candidate in candidates) {
            // Try with 1 transient retry on network drop
            var attempts = 0
            val maxAttempts = 2
            while (attempts < maxAttempts) {
                attempts++
                try {
                    val response = apiService.generateContent(
                        model = candidate,
                        apiKey = activeKey,
                        request = request
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            cachedWorkingModel = candidate
                            return GeminiResult.Success(text.trim(), modelUsed = candidate)
                        } else {
                            val blockReason = body?.promptFeedback?.blockReason
                            if (blockReason != null) {
                                return GeminiResult.Error("রেসপন্স ব্লক করা হয়েছে: $blockReason")
                            }
                        }
                    }

                    val code = response.code()
                    when (code) {
                        401, 403 -> {
                            return GeminiResult.Error("Gemini API key টি সঠিক নয় বা মেয়াদোত্তীর্ণ ($code)। সেটিংস থেকে নতুন কী দিন।", isAuthError = true)
                        }
                        429 -> {
                            return GeminiResult.Error("Gemini API ব্যবহারের কোটা শেষ ($code)। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।", isQuotaError = true)
                        }
                        404 -> {
                            // Try next candidate model immediately
                            break
                        }
                        else -> {
                            lastError = GeminiResult.Error("সার্ভার সমস্যা ($code)। অনুগ্রহ করে কিছুক্ষণ পর চেষ্টা করুন।")
                            break
                        }
                    }
                } catch (e: IOException) {
                    if (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(600)
                        continue
                    }
                    lastError = GeminiResult.Error("ইন্টারনেট সংযোগে একটু সমস্যা হচ্ছে। দয়া করে আপনার নেটওয়ার্ক কানেকশনটি একবার দেখে নিন।")
                    break
                } catch (e: Exception) {
                    lastError = GeminiResult.Error("সাময়িক সমস্যা: ${e.localizedMessage ?: "Unexpected error"}")
                    break
                }
            }
        }

        return lastError ?: GeminiResult.Error("ইন্টারনেট সংযোগ বা সার্ভারে সমস্যা হচ্ছে। অনুগ্রহ করে আপনার নেটওয়ার্ক কানেকশন চেক করুন।")
    }
}
